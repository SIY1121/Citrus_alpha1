package ui

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage

/**
 * ソフト内で頻繁に使うダイアログを生成
 */
class DialogFactory {
    companion object {
        fun buildOnProgressDialog(title : String,msg : String):Stage{
            val stage = Stage()
            stage.scene  = Scene(FXMLLoader.load<Parent>(javaClass.getResource("simpleProgressDialog.fxml")))
            stage.title = title
            stage.isResizable = false
            stage.initModality(Modality.APPLICATION_MODAL)
            (stage.scene.lookup("#label") as Label).text = msg
            val primScreenBounds = Screen.getPrimary().visualBounds
            stage.x = (primScreenBounds.width - 300) / 2
            stage.y = (primScreenBounds.height - 100) / 2
            return stage
        }
        fun ShowTestScene(){
            val stage = Stage()
            stage.scene  = Scene(FXMLLoader.load<Parent>(javaClass.getResource("testScene.fxml")))
            stage.show()
        }
    }
}