package ui

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.shape.Line
import javafx.scene.text.Font
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class Controller : Initializable {
    @FXML
    lateinit var borderPane: BorderPane
    @FXML
    lateinit var rootPane: Pane
    @FXML
    lateinit var menuBar: MenuBar
    @FXML
    lateinit var timelineLabelVBox: VBox
    @FXML
    lateinit var layerScrollPane: ScrollPane
    @FXML
    lateinit var timelineLayerVBox: VBox
    @FXML
    lateinit var gridPane: GridPane
    @FXML
    lateinit var labelScrollPane: ScrollPane
    @FXML
    lateinit var splitPane: SplitPane
    @FXML
    lateinit var timelineCaret: Line

    val LayerCount = 20

    val selectedLabel: MutableList<TimeLineObject> = ArrayList()

    var editMode = TimeLineObject.EditMode.None
    var ofx : Double = 0.0
    var ofy : Double = 0.0
    var dragging : Boolean = false

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        //レイアウトバインド
        borderPane.prefWidthProperty().bind(rootPane.widthProperty())
        borderPane.prefHeightProperty().bind(rootPane.heightProperty())
        gridPane.prefWidthProperty().bind(splitPane.widthProperty())
        labelScrollPane.vvalueProperty().bindBidirectional(layerScrollPane.vvalueProperty())

        //タイムライン生成
        for (i in 0 until LayerCount) {
            val label = Label("Layer$i")
            label.font = Font("メイリオ", 16.0)
            label.prefHeight = 30.0
            label.style = if (i % 2 == 0) "-fx-background-color: #404040" else "-fx-background-color: #353535"
            label.prefWidthProperty().bind(labelScrollPane.widthProperty())
            timelineLabelVBox.children.add(label)
            val pane = Pane()

            pane.style = if (i % 2 == 0) "-fx-background-color: #404040" else "-fx-background-color: #353535"
            pane.prefWidth = 1000.0
            pane.prefHeight = 30.0

            //コンテキストメニュー生成
            val contextMenu = ContextMenu()
            val menuItem = MenuItem("テキスト")
            menuItem.setOnAction {
                val l = TimeLineObject(objects.Shape())
                l.prefHeight = 30.0
                l.prefWidth = 100.0
                l.style = "-fx-background-color:red"
                l.setOnMousePressed {
                    selectedLabel.add(l)
                }
                l.editModeChangeListener = object : TimeLineObject.EditModeChangeListener {
                    override fun onEditModeChanged(mode: TimeLineObject.EditMode, offsetX: Double, offsetY: Double) {
                        if(dragging)return
                        editMode = mode
                        ofx = offsetX
                        ofy = offsetY
                        println(offsetX )
                    }
                }
                pane.children.add(l)
            }
            contextMenu.items.add(menuItem)
            pane.setOnMouseClicked {
                if (it.button == MouseButton.SECONDARY)
                    contextMenu.show(pane, it.screenX, it.screenY)
            }


            timelineLayerVBox.children.add(pane)
        }
        timelineCaret.endY = LayerCount * 30.0

    }

    fun mouseDragged(mouseEvent: MouseEvent) {
        dragging = true
        if (selectedLabel.size > 0)
            for (l in selectedLabel) {
                val scrollOffsetX = layerScrollPane.hvalue * (timelineLayerVBox.width - layerScrollPane.viewportBounds.width)
                val scrollOffsetY = layerScrollPane.vvalue * (timelineLayerVBox.height - layerScrollPane.viewportBounds.height) - (splitPane.dividerPositions[0] * splitPane.height) - splitPane.layoutY - 2

                when (editMode) {
                    TimeLineObject.EditMode.Move -> {
                        l.layoutX = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width - ofx
                        //l.layoutX = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width - ofy

                        if ((timelineLayerVBox.children[Math.round((mouseEvent.sceneY + scrollOffsetY) / 30).toInt()] as Pane) != (l.parent as Pane)) {
                            (l.parent as Pane).children.remove(l)
                            (timelineLayerVBox.children[Math.round((mouseEvent.sceneY + scrollOffsetY) / 30).toInt()] as Pane).children.add(l)
                        }
                    }
                    TimeLineObject.EditMode.IncrementLength -> {
                        l.prefWidth = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width - l.layoutX
                    }
                    TimeLineObject.EditMode.DecrementLength -> {
                        l.prefWidth = (l.layoutX + l.width) - (mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width)
                        l.layoutX = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width

                    }
                }
            }
        else {
            val scrollOffsetX = layerScrollPane.hvalue * (timelineLayerVBox.width - layerScrollPane.viewportBounds.width)
            timelineCaret.startX = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width
            timelineCaret.endX = mouseEvent.sceneX + scrollOffsetX - labelScrollPane.width
        }

    }

    fun mouseReleased(mouseEvent: MouseEvent) {
        for (l in selectedLabel)
            l.onLayoutUpdate()

        selectedLabel.clear()
        dragging = false
    }

    fun onVersionInfo(actionEvent: ActionEvent) {
        Alert(Alert.AlertType.NONE, "Citrus alpha 0.0.1", ButtonType.OK).show()
    }
}
