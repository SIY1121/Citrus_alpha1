package properties

import javafx.stage.FileChooser

/**
 * 選択されたファイルのパスを保持するプロパティ
 * @param filters ユーザーに選択させるファイルのフィルタ
 */
class FileProperty(val filters: List<FileChooser.ExtensionFilter>) {
    var file: String = ""
        set(value){
            field = value
            listener?.onChanged(field)
        }

    interface ChangeListener{
        fun onChanged(file : String)
    }

    var listener : ChangeListener? = null
}