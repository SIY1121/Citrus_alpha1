package ui

import com.jogamp.opengl.GLContext
import com.sun.javafx.css.StyleManager
import interpolation.BounceInterpolator
import interpolation.Interpolator
import interpolation.InterpolatorManager
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import objects.DrawableObject
import javafx.stage.Screen.getPrimary
import objects.ObjectManager
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import util.Statics
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipInputStream


class Main : Application() {
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {

        StyleManager.getInstance().addUserAgentStylesheet("/ui/main.css")

        val primScreenBounds = Screen.getPrimary().visualBounds
        val splash = Stage()
        splash.scene = Scene(FXMLLoader.load<Parent>(javaClass.getResource("splash.fxml")))
        splash.initStyle(StageStyle.UNDECORATED)
        splash.x = (primScreenBounds.width - 600) / 2
        splash.y = (primScreenBounds.height - 360) / 2
        splash.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
        splash.show()
        splash.toFront()


        Thread({
            SplashController.notifyProgress(0.1,"オブジェクトを読み込み中...")
            InterpolatorManager.load()
            SplashController.notifyProgress(0.2,"オブジェクトを読み込み中...")
            ObjectManager.load()
            SplashController.notifyProgress(0.3,"FFmpegを初期化中...")
            FFmpegFrameGrabber.tryLoad()

            val loader = FXMLLoader(javaClass.getResource("main.fxml"))
            val root = loader.load<Parent>()
            val controller = loader.getController<Controller>()
            controller.stage = primaryStage
            primaryStage.title = "Citrus"
            primaryStage.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
            primaryStage.setOnCloseRequest {
                System.exit(0)
            }

            Platform.runLater {
                SplashController.notifyProgress(1.0,"完了")
                splash.close()

                val welcomeScreen = WindowFactory.createWindow("welcome.fxml")
                welcomeScreen.title = "Citrusへようこそ"
                welcomeScreen.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
                welcomeScreen.initModality(Modality.WINDOW_MODAL)
                welcomeScreen.showAndWait()

                if(!Statics.project.initialized)System.exit(0)

                primaryStage.scene = Scene(root, 800.0, 700.0)
                primaryStage.show()
            }
        }).start()

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(Main::class.java, *args)
        }
    }
}
