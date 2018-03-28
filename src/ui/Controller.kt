package ui

import javafx.beans.InvalidationListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class Controller :Initializable {
    override fun initialize(location: URL?, resources: ResourceBundle?) {

        borderPane.prefWidthProperty().bind(rootPane.widthProperty())
        borderPane.prefHeightProperty().bind(rootPane.heightProperty())

    }

    @FXML
    lateinit var borderPane: BorderPane
    @FXML
    lateinit var rootPane: Pane
    @FXML
    lateinit var menuBar: MenuBar
    @FXML
    lateinit var splitPane: SplitPane
    @FXML
    lateinit var timelineController : TimelineController

    fun onVersionInfo(actionEvent: ActionEvent) {
        Alert(Alert.AlertType.NONE, "Citrus alpha 0.0.1", ButtonType.OK).show()
    }
}
