package ui

import com.jogamp.opengl.GLContext
import interpolation.BounceInterpolator
import interpolation.Interpolator
import interpolation.InterpolatorManager
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import objects.DrawableObject
import javafx.stage.Screen.getPrimary
import objects.ObjectManager
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


class Main : Application() {
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {

        val primScreenBounds = Screen.getPrimary().visualBounds
        val splash = Stage()
        splash.scene = Scene(FXMLLoader.load<Parent>(javaClass.getResource("splash.fxml")))
        splash.initStyle(StageStyle.UNDECORATED)
        splash.x = (primScreenBounds.width - 600) / 2
        splash.y = (primScreenBounds.height - 360) / 2
        splash.show()
        splash.toFront()


        Thread({
            SplashController.notifyProgress(0.1,"読み込み中...")
            InterpolatorManager.load()
            ObjectManager.load()
            val root = FXMLLoader.load<Parent>(javaClass.getResource("main.fxml"))
            primaryStage.title = "Citrus"
            primaryStage.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
            primaryStage.setOnCloseRequest {
                System.exit(0)
            }

            Platform.runLater {
                primaryStage.scene = Scene(root, 800.0, 700.0)
                primaryStage.show()
                SplashController.notifyProgress(1.0,"完了")
                splash.close()
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
