package ui

import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingNode
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
import javafx.stage.Stage
import jogamp.opengl.util.av.impl.FFMPEGMediaPlayer
import org.bytedeco.javacpp.avcodec
import org.bytedeco.javacpp.avformat
import org.bytedeco.javacv.FFmpegFrameGrabber
import util.Statics
import util.VideoRenderer
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

    val listener = InvalidationListener{
        val w = canvasWrapper.width - Statics.project.width.toDouble()/Statics.project.height*canvasWrapper.height
        AnchorPane.setLeftAnchor(glCanvas,w/2.0)
        AnchorPane.setRightAnchor(glCanvas,w/2.0)
    }

    fun onVersionInfo(actionEvent: ActionEvent) {

        Alert(Alert.AlertType.NONE, "Citrus alpha 0.1.0", ButtonType.OK).show()
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
            println(format.long_name().getString("ASCII"))
            format = avformat.av_oformat_next(format)
        }
    }

    fun onOutput(actionEvent: ActionEvent) {

        Thread({
            VideoRenderer.startEncode()
        }).start()
    }
}
