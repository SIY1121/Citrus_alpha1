package ui

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.web.WebView
import javafx.stage.Stage
import java.net.URL
import java.util.*

class About : Initializable {

    @FXML
    lateinit var gplView : WebView
    @FXML
    lateinit var openView : WebView

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        gplView.engine.load(javaClass.getResource("/assets/GPLv3.html").toString())
        openView.engine.load(javaClass.getResource("/assets/Library.html").toString())
    }

    fun onClicked(actionEvent: ActionEvent) {
        (gplView.scene.window as Stage).close()
    }
}