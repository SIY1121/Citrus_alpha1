package ui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class Main : Application() {



    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("main.fxml"))
        primaryStage.title = "Citrus"
        primaryStage.icons.add(Image(javaClass.getResourceAsStream("/assets/icon.png")))
        primaryStage.scene = Scene(root, 500.0, 500.0)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(Main::class.java,*args)
        }
    }
}
