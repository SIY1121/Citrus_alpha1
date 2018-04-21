package ui

import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.Stage
import org.bytedeco.javacpp.avcodec
import org.bytedeco.javacpp.avformat
import util.Statics
import util.VideoRenderer
import java.net.URL
import java.util.*

class Controller : Initializable {

    @FXML
    lateinit var borderPane: BorderPane
    @FXML
    lateinit var rootPane: Pane
    @FXML
    lateinit var menuBar: MenuBar
    @FXML
    lateinit var splitPane: SplitPane
    @FXML
    lateinit var timelineController: TimelineController
    @FXML
    lateinit var glCanvas: SwingNode
    @FXML
    lateinit var canvasWrapper : Pane
    @FXML
    lateinit var rightPane : AnchorPane


    lateinit var canvas : GlCanvas

    lateinit var welcomeScreen : Stage

    var stage : Stage? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("initialize")
        canvas = GlCanvas()
        timelineController.parentController = this
        timelineController.glCanvas = canvas

        borderPane.prefWidthProperty().bind(rootPane.widthProperty())
        borderPane.prefHeightProperty().bind(rootPane.heightProperty())
        canvasWrapper.heightProperty().addListener(listener)
        canvasWrapper.widthProperty().addListener(listener)

        SplashController.notifyProgress(0.7,"OpenGLを初期化中...")

        glCanvas.content = canvas
    }

    fun showWelcomeScreen(){
        welcomeScreen = WindowFactory.createWindow("welcome.fxml")
        welcomeScreen.initOwner(stage)
        welcomeScreen.initModality(Modality.WINDOW_MODAL)
        welcomeScreen.showAndWait()
    }

    val listener = InvalidationListener{
        val w = canvasWrapper.width - Statics.project.width.toDouble()/Statics.project.height*canvasWrapper.height
        AnchorPane.setLeftAnchor(glCanvas,w/2.0)
        AnchorPane.setRightAnchor(glCanvas,w/2.0)
    }

    fun onVersionInfo(actionEvent: ActionEvent) {
        val stage = Stage()
        stage.scene  = Scene(FXMLLoader.load<Parent>(javaClass.getResource("about.fxml")))
        stage.isResizable = false
        stage.title = "Citrusについて"
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.show()
    }

    fun onCodecInfo(actionEvent: ActionEvent) {
        avcodec.avcodec_register_all()
        var codec = avcodec.av_codec_next(null)
        while (codec!=null)
        {
            println(codec.long_name().getString("ASCII") + " - " + codec.name().getString("ASCII") + " - " + codec.id() + "/"+codec.type())
            codec = avcodec.av_codec_next(codec)
        }
        avformat.av_register_all()
        println("--format--")
        var format = avformat.av_oformat_next(null)
        while (format!=null){
            try{
                println(format.long_name().getString("ASCII") + " " + format.mime_type().getString("ASCII"))
                //if(format.mime_type().getString("ASCII").contains("image/")){
                //    format.extensions().getString("ASCII").split(",").forEach { print("\"$it\",") }
                //}
            }catch (ex : Exception){
                //ex.printStackTrace()
            }

            format = avformat.av_oformat_next(format)
        }
    }

    fun onOutput(actionEvent: ActionEvent) {

        Thread({
            VideoRenderer.startEncode()
        }).start()
    }

    fun onTest(actionEvent: ActionEvent) {
        WindowFactory.ShowTestScene()
    }
}
