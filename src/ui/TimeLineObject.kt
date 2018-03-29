package ui

import annotation.CObject
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import objects.CitrusObject
import annotation.CProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import com.sun.javafx.scene.control.skin.TitledPaneSkin
import javafx.collections.ObservableList
import javafx.scene.layout.HBox
import javafx.util.Duration
import objects.MutableProperty
import objects.SelectableProperty
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.jvm.jvmErasure


class TimeLineObject(var cObject: CitrusObject) : VBox(),
        CitrusObject.DisplayNameChangeListener {

    /**
     * セクション名とプロパティのリストを持つデータクラス
     * @param group グループ名
     * @param property 初期化するリスト
     */
    data class PropertySection(val group: String, val property: MutableList<KProperty1<CitrusObject, *>> = ArrayList())

    /**
     * 表示用ラベル
     */
    val label = Label()

    /**
     * 編集コントロールを表示するウィンドウ
     */
    val popup = PopupControl()

    /**
     * ポップアップウィンドウのルート要素
     */
    var popupRoot: VBox

    /**
     * セクションのリスト
     */
    val properties: MutableList<PropertySection> = ArrayList()


    /**
     * カーソルの位置から
     * 編集モードを変更、通知する
     */
    private val mouseMove = EventHandler<MouseEvent> {
        when {
            it.x < 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.DecrementLength, it.x, it.y)
            }
            it.x > width - 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.IncrementLength, it.x, it.y)
            }
            else -> {
                scene.cursor = Cursor.DEFAULT
                editModeChangeListener?.onEditModeChanged(EditMode.Move, it.x, it.y)
            }
        }
    }

    /**
     * カーソルが出た場合に
     * 標準に戻す
     */
    private val mouseExited = EventHandler<MouseEvent> {
        scene.cursor = Cursor.DEFAULT
    }

    /**
     * ポップアップウィンドウ表示
     */
    private val mouseClicked = EventHandler<MouseEvent> {
        if (it.button == MouseButton.SECONDARY) {

            popup.show(this, it.screenX, it.screenY)
            it.consume()
            println("hi")

        } else {
            popup.hide()
        }
    }

    init {
        cObject.displayNameChangeListener = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
        onMouseClicked = mouseClicked
        label.maxWidthProperty().bind(widthProperty())
        children.add(label)

        popupRoot = VBox()
        popupRoot.style = "-fx-base: #323232;-fx-background-color:#383838AA;-fx-border-radius: 5 5 5 5;-fx-background-radius: 5 5 5 5;"
        popupRoot.effect = DropShadow()
        popupRoot.padding = Insets(10.0)


        //cObjectの親クラスをたどり、それぞれのプロパティを取得
        cObject.javaClass.kotlin.allSuperclasses.reversed().forEach { clazz ->
            //これらのクラスは除外
            if (clazz != Any::class && clazz != CitrusObject::class) {
                val section = PropertySection(
                        if (clazz.annotations.isNotEmpty() && clazz.annotations[0] is CObject)
                            (clazz.annotations[0] as CObject).name
                        else
                            "無題"
                )
                //CPropertyアノテーションを持ったプロパティのみ登録
                clazz.memberProperties.filter { it.annotations.isNotEmpty() && it.annotations[0] is CProperty }
                        .forEach { p ->
                            section.property.add(cObject.javaClass.kotlin.memberProperties.first { p.name == it.name })
                        }
                properties.add(section)
            }
        }

        //cObject本体のプロパティを登録
        val section = PropertySection(
                if (cObject.javaClass.kotlin.annotations.isNotEmpty() && cObject.javaClass.kotlin.annotations[0] is CObject)
                    (cObject.javaClass.kotlin.annotations[0] as CObject).name
                else
                    "無題")
        cObject.javaClass.kotlin.declaredMemberProperties.filter { it.annotations.isNotEmpty() && it.annotations[0] is CProperty }
                .forEach {
                    section.property.add(it)
                }
        properties.add(section)

        //取得したプロパティからUIを生成
        for (p in properties) {
            val vbox = VBox()
            val accordion = TitledPane(p.group, vbox)
            accordion.isAnimated = false

            //CPropertyアノテーションのindexに基づいてソート
            p.property.sortWith(Comparator { o1, o2 -> (o1.annotations[0] as CProperty).index - (o2.annotations[0] as CProperty).index })

            for (pp in p.property) {
                val name = (pp.annotations[0] as CProperty).displayName
                val v = pp.get(cObject)
                when(v){
                    is MutableProperty->{
                        val hbox = HBox()
                        hbox.children.add(Label(name))
                        val slider = Slider()
                        slider.min = v.min
                        slider.max = v.max
                        slider.value = v.value(1)

                        hbox.children.add(slider)

                        vbox.children.add(hbox)
                    }
                    is SelectableProperty->{
                        val hbox = HBox()
                        hbox.children.add(Label(name))
                        val choice = ChoiceBox<String>()
                        choice.items.addAll(v.list)
                        choice.setOnAction { v.selectedIndex = choice.selectionModel.selectedIndex }
                        hbox.children.add(choice)

                        vbox.children.add(hbox)
                    }
                }

            }
            popupRoot.children.add(accordion)
        }

        val b = Button("OK")
        b.setOnAction { popup.hide() }
        popupRoot.children.add(b)


        popup.scene.root = popupRoot
    }

    override fun onDisplayNameChanged(name: String) {
        label.text = name
    }

    enum class EditMode {
        None, Move, IncrementLength, DecrementLength
    }

    interface EditModeChangeListener {
        fun onEditModeChanged(mode: EditMode, offsetX: Double, offsetY: Double)
    }

    var editModeChangeListener: EditModeChangeListener? = null

    fun onMove() {
        cObject.start = (layoutX / TimelineController.pixelPerFrame).toInt()
        cObject.end = ((layoutX + width) / TimelineController.pixelPerFrame).toInt()


        cObject.onLayoutUpdate()
    }

    fun onScaleChanged() {
        layoutX = cObject.start * TimelineController.pixelPerFrame
        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX
    }

}