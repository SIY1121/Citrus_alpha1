package ui

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import objects.DrawableObject

class Main : Application() {
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {


        println("start")
        val splash = Stage()
        splash.scene = Scene(FXMLLoader.load<Parent>(javaClass.getResource("splash.fxml")))
        splash.initStyle(StageStyle.UNDECORATED)
        splash.show()
        splash.toFront()


        Thread({
            println("loadmain")
            val root = FXMLLoader.load<Parent>(javaClass.getResource("main.fxml"))
            primaryStage.title = "Citrus"
            primaryStage.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
            println("scene")
            Platform.runLater {
                primaryStage.scene = Scene(root, 500.0, 500.0)
                primaryStage.show()
                println("show")
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
