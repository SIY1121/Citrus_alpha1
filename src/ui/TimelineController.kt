package ui

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
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


    var layerCount = 0
    val layerHeight = 30.0

    companion object {

        var pixelPerFrame = 2.0
    }
    var tick = 30

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

        labelScrollPane.vvalueProperty().bindBidirectional(layerScrollPane.vvalueProperty())
        timelineRootPane.widthProperty().addListener({ _, _, n ->
            timelineAxis.width = n.toDouble() - 80
            drawAxis()
        })
        scaleSlider.valueProperty().addListener({ _, _, n ->
            pixelPerFrame = n.toDouble()

            for(pane in layerVBox.children)
                if(pane is Pane)
                for(o in pane.children){
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
        layerScrollPane.setOnKeyPressed { it.consume() }


        sceneChoiceBox.items.addAll(arrayOf("Root", "Scene1", "Scene2", "Scene3"))

        for (i in 0..10)
            generateLayer()

        hScrollBar.requestLayout()
    }

    fun generateLayer() {
        //ラベル生成
        val label = Label("Layer${layerCount + 1}")
        label.font = Font(15.0)
        label.minHeight = layerHeight
        label.prefWidth = 80.0
        label.style = "-fx-background-color:" + if (layerCount % 2 == 0) "#343434" else "#383838"
        labelVBox.children.add(label)
        //レイヤーペイン生成
        val pane = Pane()
        pane.minHeight = layerHeight
        pane.minWidth = 2000.0
        pane.style = "-fx-background-color:" + if (layerCount % 2 == 0) "#343434" else "#383838"

        //サブメニュー
        val menu = ContextMenu()
        val menuObject = Menu("オブジェクトの追加")
        menu.items.add(menuObject)
        val menuShape = MenuItem("図形")
        val thisLayer = layerCount
        menuShape.setOnAction {
            val o = TimeLineObject(Shape())
            o.prefHeight = layerHeight
            o.style = "-fx-background-color:red"
            o.prefWidth = 200.0
            o.cObject.layer = thisLayer
            o.setOnMousePressed {
                selectedObjects.add(o)
                //selectedObjectsOldX.add(o.layoutX)
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

            pane.children.add(o)
        }
        menuObject.items.add(menuShape)
        pane.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY)
                menu.show(pane, it.screenX, it.screenY)
        }

        layerVBox.children.add(pane)

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
            g.fillText("${i * tick}f", x, 20.0)
            g.strokeLine(x, 20.0, x, 35.0)
        }
    }

    fun LayerScrollPane_onMousePressed(mouseEvent: MouseEvent) {

        selectedOrigin = mouseEvent.x
        dragging = true

        if(selectedObjects.isEmpty() && mouseEvent.button==MouseButton.PRIMARY)
            caret.layoutX = mouseEvent.x
    }

    fun LayerScrollPane_onMouseDragged(mouseEvent: MouseEvent) {
        if (selectedObjects.isNotEmpty())
            for ((i, o) in selectedObjects.withIndex()) {
                when (editMode) {
                    TimeLineObject.EditMode.Move -> {
                        o.layoutX = mouseEvent.x - selectedOffsetX

                        if (layerVBox.children[(mouseEvent.y / layerHeight).toInt()] != o.parent) {
                            (o.parent as Pane).children.remove(o)
                            o.cObject.layer = (mouseEvent.y / layerHeight).toInt()
                            (layerVBox.children[(mouseEvent.y / layerHeight).toInt()] as Pane).children.add(o)
                        }

                    }
                    TimeLineObject.EditMode.IncrementLength ->
                        o.prefWidth = mouseEvent.x - o.layoutX
                    TimeLineObject.EditMode.DecrementLength -> {
                        o.layoutX = mouseEvent.x
                        o.prefWidth = (selectedOrigin - mouseEvent.x) + selectedObjectOldWidth[i] - selectedOffsetX
                    }
                }
            }
        else{
            caret.layoutX = mouseEvent.x
        }

    }

    fun LayerScrollPane_onMouseReleased(mouseEvent: MouseEvent) {
        dragging = false

        for(o in selectedObjects)
            o.onMove()

        selectedObjects.clear()
        selectedObjectOldWidth.clear()
    }

}