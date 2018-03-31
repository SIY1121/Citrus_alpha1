package ui

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import objects.Shape
import util.Statics
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class TimelineController : Initializable {
    @FXML
    lateinit var labelVBox: VBox
    @FXML
    lateinit var layerScrollPane: ScrollPane
    @FXML
    lateinit var layerVBox: VBox
    @FXML
    lateinit var timelineRootPane: GridPane
    @FXML
    lateinit var labelScrollPane: ScrollPane
    @FXML
    lateinit var caret: Line
    @FXML
    lateinit var timelineAxis: Canvas
    @FXML
    lateinit var scaleSlider: Slider
    @FXML
    lateinit var hScrollBar: ScrollBar
    @FXML
    lateinit var sceneChoiceBox: ChoiceBox<String>

    lateinit var glCanvas: GlCanvas

    lateinit var parentController : Controller

    var layerCount = 0
    val layerHeight = 30.0

    companion object {

        var pixelPerFrame = 2.0
    }

    var tick = Statics.project.fps

    val offsetX: Double
        get() = layerScrollPane.hvalue * (layerVBox.width - layerScrollPane.viewportBounds.width)

    var selectedObjects: MutableList<TimeLineObject> = ArrayList()
    //var selectedObjectsOldX : MutableList<Double> = ArrayList()
    var selectedObjectOldWidth: MutableList<Double> = ArrayList()
    var dragging = false
    var selectedOffsetX = 0.0
    var selectedOrigin = 0.0
    var editMode = TimeLineObject.EditMode.None

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        SplashController.notifyProgress(0.3,"UIを初期化中...")

        labelScrollPane.vvalueProperty().bindBidirectional(layerScrollPane.vvalueProperty())
        timelineRootPane.widthProperty().addListener({ _, _, n ->
            timelineAxis.width = n.toDouble() - 80
            drawAxis()
        })
        scaleSlider.valueProperty().addListener({ _, _, n ->
            pixelPerFrame = n.toDouble()

            for (pane in layerVBox.children)
                if (pane is Pane)
                    for (o in pane.children) {
                        (o as? TimeLineObject)?.onScaleChanged()
                    }

            drawAxis()
        })

        hScrollBar.minProperty().bind(layerScrollPane.hminProperty())
        hScrollBar.maxProperty().bind(layerScrollPane.hmaxProperty())
        //hScrollBar.visibleAmountProperty().bind(layerScrollPane.widthProperty().divide(layerScrollPane.heightProperty()))
        layerScrollPane.hvalueProperty().bindBidirectional(hScrollBar.valueProperty())

        layerScrollPane.hvalueProperty().addListener({ _, _, n ->
            drawAxis()
        })
        layerScrollPane.setOnKeyPressed {
            when(it.code){
                KeyCode.SPACE->{
                    if(!playing)play()
                    else stop()
                }
                KeyCode.RIGHT->{
                    glCanvas.currentFrame++
                    caret.layoutX =  glCanvas.currentFrame * pixelPerFrame
                }
                KeyCode.LEFT->{
                    glCanvas.currentFrame--
                    caret.layoutX =  glCanvas.currentFrame * pixelPerFrame
                }
            }
            it.consume()
        }


        sceneChoiceBox.items.addAll(arrayOf("Root", "Scene1", "Scene2", "Scene3"))

        for (i in 0..10)
            generateLayer()

        hScrollBar.requestLayout()
    }

    fun generateLayer() {

        //レイヤーペイン生成
        val layerPane = Pane()
        layerPane.minHeight = layerHeight
        layerPane.maxHeight = layerHeight
        layerPane.minWidth = 2000.0
        layerPane.style = "-fx-background-color:" + if (layerCount % 2 == 0) "#343434" else "#383838"

        //サブメニュー
        val menu = ContextMenu()
        val menuObject = Menu("オブジェクトの追加")
        menu.items.add(menuObject)
        val menuShape = MenuItem("図形")
        val thisLayer = layerCount
        menuShape.setOnAction {
            val cObject = Shape()
            cObject.layer = thisLayer
            val o = TimeLineObject(cObject,this)
            o.prefHeight = layerHeight * 2
            o.style = "-fx-background-color:red"
            o.prefWidth = 200.0
            o.setOnMousePressed {
                selectedObjects.add(o)
                selectedObjectOldWidth.add(o.width)
            }
            o.editModeChangeListener = object : TimeLineObject.EditModeChangeListener {
                override fun onEditModeChanged(mode: TimeLineObject.EditMode, offsetX: Double, offsetY: Double) {
                    if (!dragging) {
                        editMode = mode
                        selectedOffsetX = offsetX
                    }
                }
            }

            layerPane.children.add(o)
            layerScrollPane.layout()
        }
        menuObject.items.add(menuShape)
        layerPane.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY)
                menu.show(layerPane, it.screenX, it.screenY)
        }

        //label.minHeightProperty().bind(pane.heightProperty())
        //pane.heightProperty().addListener({_,_,n->println(n)})

        layerVBox.children.add(layerPane)
        //ラベル生成
        val labelPane = VBox()
        labelPane.minHeight = layerHeight
        labelPane.maxHeight = layerHeight
        labelPane.style = "-fx-background-color:" + if (layerCount % 2 == 0) "#343434" else "#383838"
        labelVBox.children.add(labelPane)

        val label = Label("Layer${layerCount + 1}")
        label.font = Font(15.0)
        label.prefWidth = 80.0
        label.minHeight = 20.0
        labelPane.children.add(label)

        val toggle = ToggleButton()
        toggle.maxHeight = 10.0
        toggle.minWidth = 30.0
        toggle.style = "-fx-font-size:2px"
        toggle.setOnAction {
            layerScrollPane.requestFocus()
            if (toggle.isSelected) {
                layerPane.maxHeight = Double.POSITIVE_INFINITY
                layerScrollPane.layout()//TODO LabelPaneのサイズ変更がおくれる原因の調査
            } else {
                layerPane.maxHeight = layerHeight
                layerScrollPane.layout()
            }
        }
        labelPane.children.add(toggle)

        labelPane.minHeightProperty().bind(layerPane.heightProperty())

        layerCount++

        Statics.project.Layer.add(ArrayList())
        caret.endY = layerCount * layerHeight
    }

    fun drawAxis() {
        val g = timelineAxis.graphicsContext2D
        g.clearRect(0.0, 0.0, g.canvas.width, g.canvas.height)
        g.fill = Color.WHITE
        g.stroke = Color.WHITE
        g.font = Font(10.0)
        for (i in 0..20) {
            val x = i * tick * pixelPerFrame - offsetX
            g.fillText("${i * tick / Statics.project.fps}s", x, 20.0)
            g.strokeLine(x, 20.0, x, 35.0)
        }
    }

    fun LayerScrollPane_onMousePressed(mouseEvent: MouseEvent) {
        if (mouseEvent.button != MouseButton.PRIMARY) return
        selectedOrigin = mouseEvent.x
        dragging = true

        if (selectedObjects.isEmpty() && mouseEvent.button == MouseButton.PRIMARY) {
            caret.layoutX = mouseEvent.x
            glCanvas.currentFrame = (caret.layoutX / pixelPerFrame).toInt()
        }
    }

    fun LayerScrollPane_onMouseDragged(mouseEvent: MouseEvent) {
        if (mouseEvent.button != MouseButton.PRIMARY) return
        if (selectedObjects.isNotEmpty())
            for ((i, o) in selectedObjects.withIndex()) {
                when (editMode) {
                    TimeLineObject.EditMode.Move -> {
                        o.layoutX = mouseEvent.x - selectedOffsetX

                        if (layerVBox.children[(mouseEvent.y / layerHeight).toInt()] != o.parent) {
                            val src = (o.parent as Pane)
                            val dst = (layerVBox.children[(mouseEvent.y / layerHeight).toInt()] as Pane)
                            val srcIndex = o.cObject.layer
                            val dstIndex = (mouseEvent.y / layerHeight).toInt()

                            src.children.remove(o)
                            dst.children.add(o)

                            o.onLayerChanged(srcIndex, dstIndex)

                            layerScrollPane.layout()
                        }

                    }
                    TimeLineObject.EditMode.IncrementLength ->
                        o.prefWidth = mouseEvent.x - o.layoutX
                    TimeLineObject.EditMode.DecrementLength -> {
                        o.layoutX = mouseEvent.x
                        o.prefWidth = (selectedOrigin - mouseEvent.x) + selectedObjectOldWidth[i] - selectedOffsetX
                    }
                    TimeLineObject.EditMode.None -> {
                        //Nothing to do
                    }
                }
            }
        else {
            caret.layoutX = mouseEvent.x
            glCanvas.currentFrame = (caret.layoutX / pixelPerFrame).toInt()
        }

    }

    fun LayerScrollPane_onMouseReleased(mouseEvent: MouseEvent) {
        if (mouseEvent.button != MouseButton.PRIMARY) return

        dragging = false

        for (o in selectedObjects)
            o.onMoved()

        if (selectedObjects.isNotEmpty()) {
            glCanvas.currentObjects.clear()
            glCanvas.currentFrame = glCanvas.currentFrame
            println("${glCanvas.currentObjects.size}")
        }

        selectedObjects.clear()
        selectedObjectOldWidth.clear()
    }


    var playing = false
    fun play(){
        playing=true
        val start = System.currentTimeMillis()
        val startFrame = glCanvas.currentFrame
        Thread({
            while (playing){
                glCanvas.currentFrame = startFrame + ((System.currentTimeMillis() - start)/(1000.0/Statics.project.fps)).toInt()
                Platform.runLater {caret.layoutX =  glCanvas.currentFrame * pixelPerFrame  }
                Thread.sleep(10)
            }
        }).start()
    }

    fun stop(){
        playing=false
    }

}