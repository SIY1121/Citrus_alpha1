package ui

import annotation.CObject
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import annotation.CProperty
import interpolation.BounceInterpolator
import interpolation.Interpolator
import interpolation.InterpolatorManager
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import objects.*
import properties.*
import util.Settings
import util.Statics


class TimeLineObject(var cObject: CitrusObject, val timelineController: TimelineController) : VBox(),
        CitrusObject.DisplayNameChangeListener {

    /**
     * プロパティとUIを一括管理するためのクラス
     * @param kProprety リフレクションから取得したプロパティの生データ
     * @param property MutableProperty等のそのまま参照できるプロパティ
     * @param node スライダー等の値調整用UI
     * @param pane タイムライン上でキーフレームを表示する親Pane
     */
    data class PropertyData(val kProprety: KProperty1<CitrusObject, *>, var property: Any?, var node: Node?, var pane: Pane?)

    /**
     * セクション名とプロパティのリストを持つデータクラス
     * @param group グループ名
     * @param property 初期化するリスト
     */
    data class PropertySection(val group: String, val property: MutableList<PropertyData> = ArrayList())

    /**
     * 表示用ラベル
     */
    val label = Label()

    /**
     * アイコン表示用
     */
    val imageView = ImageView()

    /**
     * ヘッダ類をまとめる親
     */
    val headerPane = Pane()

    /**
     * 表示用のラベル、アイコン等をまとめる親
     */
    val infoPane = HBox()

    var color = Color.RED

    /**
     * サブメニューポップアップ
     */
    val popup = PopupControl()

    /**
     * サブメニュールート
     */
    val popupRoot = VBox()

    /**
     * 編集画面のルート
     */
    lateinit var editWindowRoot: VBox

    /**
     * セクションのリスト
     */
    val properties: MutableList<PropertySection> = ArrayList()

    private var currentFrame = 0

    var strictSelected = false
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
     * クリック時の動作
     */
    private val mouseClicked = EventHandler<MouseEvent> {
        if (it.button == MouseButton.PRIMARY) {
            timelineController.parentController.rightPane.children.clear()
            timelineController.parentController.rightPane.children.add(editWindowRoot)
            AnchorPane.setRightAnchor(editWindowRoot, 0.0)
            AnchorPane.setLeftAnchor(editWindowRoot, 0.0)
        } else if (it.button == MouseButton.SECONDARY) {

            popup.show(this, it.screenX, it.screenY)
            it.consume()
        }

    }

    init {
        cObject.displayNameChangeListener = this
        cObject.uiObject = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
        onMouseClicked = mouseClicked

        label.text = cObject.displayName

        label.minHeight = 30.0
        //label.textFill = Color.BLACK
        label.effect = DropShadow()
        val iconUrl = (cObject.javaClass.kotlin.annotations.first { it is CObject } as CObject).iconUrl
        if (iconUrl.isNotBlank()) {
            imageView.fitHeight = 30.0
            imageView.fitWidth = 30.0
            imageView.isPreserveRatio = true
            imageView.image = Image(iconUrl)
            infoPane.children.add(imageView)
        }

        val color = (cObject.javaClass.kotlin.annotations.first { it is CObject } as CObject).color
        this.color = Color.web(color)

        infoPane.children.add(label)
        infoPane.maxWidthProperty().bind(widthProperty())

        widthProperty().addListener({ _, _, n ->
            if (n.toDouble() < 50) {
                imageView.isVisible = false
                label.isVisible = false
            } else {
                imageView.isVisible = true
                label.isVisible = true
            }
        })

        headerPane.children.add(infoPane)
        headerPane.minHeight = 30.0
        headerPane.maxWidthProperty().bind(widthProperty())

        children.add(headerPane)

        editWindowRoot = VBox()
        editWindowRoot.style = "-fx-base: #323232;-fx-background-color:#383838AA;-fx-border-radius: 5 5 5 5;-fx-background-radius: 5 5 5 5;"
        editWindowRoot.effect = DropShadow()
        editWindowRoot.padding = Insets(10.0)


        //cObjectの親クラスをたどり、それぞれのプロパティを取得
        cObject.javaClass.kotlin.allSuperclasses.reversed().filter { !it.java.isInterface }.forEach { clazz ->
            //これらのクラスは除外
            if (clazz != Any::class && clazz != CitrusObject::class) {
                val section = PropertySection(
                        if (clazz.annotations.any { it is CObject })
                            (clazz.annotations.first { it is CObject } as CObject).name
                        else
                            "無題"
                )
                //CPropertyアノテーションを持ったプロパティのみ登録
                clazz.memberProperties.filter { it.annotations.any { it is CProperty } }
                        .forEach { p ->
                            section.property.add(PropertyData(cObject.javaClass.kotlin.memberProperties.first { p.name == it.name }, null, null, null))
                        }
                properties.add(section)
            }
        }

        //cObject本体のプロパティを登録
        val section = PropertySection(
                if (cObject.javaClass.annotations.any { it is CObject })
                    (cObject.javaClass.kotlin.annotations.first { it is CObject } as CObject).name
                else
                    "無題")
        cObject.javaClass.kotlin.declaredMemberProperties.filter { it.annotations.any { it is CProperty } }
                .forEach {
                    section.property.add(PropertyData(it, null, null, null))
                }
        properties.add(section)

        //取得したプロパティからUIを生成
        for (p in properties) {
            val grid = GridPane()
            val accordion = TitledPane(p.group, grid)
            grid.columnConstraints.add(ColumnConstraints())
            grid.columnConstraints.add(ColumnConstraints())
            grid.prefWidthProperty().bind(accordion.widthProperty())
            grid.hgap = 10.0
            grid.vgap = 10.0
            accordion.isAnimated = false

            //CPropertyアノテーションのindexに基づいてソート
            //p.property.sortWith(Comparator { o1, o2 -> (o1.kProprety.annotations[0] as CProperty).index - (o2.kProprety.annotations[0] as CProperty).index })
            p.property.sortBy { (it.kProprety.annotations.first { it is CProperty } as CProperty).index }

            for ((i, pp) in p.property.withIndex()) {
                val name = (pp.kProprety.annotations.first { it is CProperty } as CProperty).displayName
                val v = pp.kProprety.get(cObject)
                pp.property = v
                when (v) {
                    is MutableProperty -> {

                        grid.add(Label(name), 0, i)
                        val slider = CustomSlider()
                        slider.min = v.min
                        slider.max = v.max
                        slider.value = v.value(1)
                        slider.tick = v.tick
                        slider.valueProperty.addListener({ _, _, n ->
                            //キーフレームがない場合
                            if (v.keyFrames.size == 0) {
                                v.fixedValue = n.toDouble()
                            } else {//キーフレームがある場合
                                v.temporaryValue = n.toDouble()
                                v.temporaryMode = true
                            }
                        })

                        //予めキーフレーム用のPaneは追加しておく
                        val pane = Pane()
                        pane.prefHeight = 0.0
                        pane.style = "-fx-background-color:yellow"
                        pane.isVisible = false
                        children.add(pane)//キーフレーム用のPaneを確保
                        pp.pane = pane

                        //キーフレーム追加用コード
                        slider.keyPressedOnHoverListener = object : CustomSlider.KeyPressedOnHover {
                            override fun onKeyPressed(it: KeyEvent) {
                                println("key ${it.code}")
                                if (it.code == KeyCode.I) {
                                    println("added:${v.getKeyFrameIndex(currentFrame) + 1},$currentFrame ,${slider.value}")

                                    if (v.keyFrames.size == 0) {//初追加の場合
                                        pane.isVisible = true//表示
                                        pane.minHeight = 10.0
                                    }

                                    val keyFrameIndex = v.isKeyFrame(currentFrame)
                                    if (keyFrameIndex == -1) {
                                        val keyFrame = MutableProperty.KeyFrame(currentFrame, BounceInterpolator(), slider.value)
                                        v.keyFrames.add(v.getKeyFrameIndex(currentFrame) + 1, keyFrame)
                                        val circle = Circle()
                                        circle.layoutY = 5.0
                                        circle.radius = 5.0
                                        circle.fill = Paint.valueOf("BLUE")
                                        circle.layoutX = TimelineController.pixelPerFrame * currentFrame

                                        circle.setOnMouseEntered {

                                            scene.cursor = Cursor.HAND
                                            it.consume()
                                        }
                                        circle.setOnMouseMoved { it.consume() }
                                        circle.setOnMouseExited {
                                            scene.cursor = Cursor.DEFAULT
                                            it.consume()
                                        }

                                        circle.setOnMouseDragged {
                                            circle.layoutX = circle.localToParent(it.x, it.y).x
                                            it.consume()
                                        }
                                        circle.setOnMouseReleased {
                                            keyFrame.frame = (circle.layoutX / TimelineController.pixelPerFrame).toInt()
                                            v.keyFrames.sortBy { it.frame }
                                            it.consume()
                                        }
                                        circle.setOnMousePressed {
                                            //押されたキーフレームに移動
                                            timelineController.glCanvas.currentFrame = keyFrame.frame + cObject.start
                                            timelineController.caret.layoutX = timelineController.glCanvas.currentFrame * TimelineController.pixelPerFrame
                                            it.consume()
                                        }

                                        val contextMenu = ContextMenu()
                                        for (i in InterpolatorManager.interpolator) {
                                            val menu = MenuItem(i.key)
                                            menu.setOnAction {
                                                keyFrame.interpolation = (i.value.newInstance() as Interpolator)
                                            }
                                            contextMenu.items.add(menu)
                                        }
                                        circle.setOnMouseClicked {
                                            if (it.button == MouseButton.SECONDARY) {
                                                contextMenu.show(circle, it.screenX, it.screenY)
                                                it.consume()
                                            }
                                        }



                                        pp.pane?.children?.add(circle)

                                        slider.style = "-fx-base:#FFFF00"
                                        println(v.keyFrames.last().value)
                                    } else {
                                        v.keyFrames[keyFrameIndex].value = slider.value
                                    }


                                }
                            }
                        }
                        //GridPane.setMargin(slider, Insets(5.0))
                        grid.add(slider, 1, i)
                        pp.node = slider
                    }
                    is SelectableProperty -> {
                        grid.add(Label(name), 0, i)
                        val choice = ChoiceBox<String>()
                        choice.items.addAll(v.list)
                        choice.setOnAction { v.selectedIndex = choice.selectionModel.selectedIndex }
                        grid.add(choice, 1, i)
                    }

                    is FileProperty -> {
                        grid.add(Label(name), 0, i)
                        val button = Button("ファイルを選択")
                        button.setOnAction {
                            val chooser = FileChooser()
                            chooser.title = "ファイルを選択"
                            chooser.extensionFilters.addAll(v.filters)
                            v.file = chooser.showOpenDialog(this.scene.window).path
                        }
                        grid.add(button, 1, i)

                    }
                    is SwitchableProperty -> {
                        grid.add(Label(name), 0, i)
                        val checkBox = CheckBox("")
                        checkBox.isSelected = v.value
                        checkBox.setOnAction {
                            v.value = checkBox.isSelected
                        }
                        grid.add(checkBox, 1, i)
                    }
                    is TextProperty -> {
                        grid.add(Label(name), 0, i)
                        val textArea = TextArea(v.text)
                        textArea.textProperty().addListener { _, _, n ->
                            v.text = n.toString()
                        }
                        grid.add(textArea, 1, i)
                    }
                    is ColorProperty -> {
                        grid.add(Label(name), 0, i)
                        val colorPicker = ColorPicker()
                        colorPicker.setOnAction { v.color = colorPicker.value }
                        grid.add(colorPicker, 1, i)
                    }
                }

            }
            grid.columnConstraints[1].hgrow = Priority.ALWAYS
            editWindowRoot.children.add(accordion)
        }
        setupMenu()
    }

    private fun setupMenu() {
        val headerGrid = GridPane()
        headerGrid.columnConstraints.addAll(ColumnConstraints(), ColumnConstraints())
        headerGrid.rowConstraints.addAll(RowConstraints(), RowConstraints())
        headerGrid.columnConstraints[1].hgrow = Priority.ALWAYS
        headerGrid.columnConstraints[0].minWidth = 25.0
        headerGrid.columnConstraints[0].isFillWidth = true
        headerGrid.rowConstraints[0].isFillHeight = true
        headerGrid.hgap = 1.0
        val headerColorBlock = Pane()

        headerColorBlock.style = "-fx-background-color:#${color.toString().substring(2)}"
        GridPane.setRowSpan(headerColorBlock, 2)
        val headerLabel = Label()
        headerLabel.text = cObject.displayName
        headerLabel.textFill = Color.WHITE

        val headerControl = HBox()
        val slider = CustomSlider()
        slider.min = 0.0
        slider.minWidth = 100.0
        slider.name = "長さ"
        slider.valueProperty.addListener { _, _, n ->
            cObject.end = cObject.start + n.toInt()
            onScaleChanged()
        }
        headerControl.children.add(slider)


        headerGrid.add(headerColorBlock, 0, 0)
        headerGrid.add(headerLabel, 1, 0)
        headerGrid.add(headerControl, 1, 1)
        popupRoot.children.add(headerGrid)

        popupRoot.children.add(Label("コピー"))
        val divideLabel = Label("分割")
        divideLabel.setOnMouseClicked {
            timelineController.addObject(cObject.javaClass,cObject.layer,null,timelineController.glCanvas.currentFrame,cObject.end)
            cObject.end = timelineController.glCanvas.currentFrame
            onScaleChanged()
        }
        popupRoot.children.add(divideLabel)

        popupRoot.spacing = 4.0
        popupRoot.style = "-fx-base: #323232;-fx-background-color:#383838AA;-fx-border-color:white;-fx-text-fill: white;"
        popupRoot.effect = DropShadow()
        popupRoot.padding = Insets(2.0)
        popup.scene.root = popupRoot
        popup.isAutoHide = true

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

    fun onLayerChanged(old: Int, new: Int) {
        cObject.layer = new
    }

    fun onMoved(mode: EditMode) {
        cObject.start = (layoutX / TimelineController.pixelPerFrame).toInt()
        cObject.end = ((layoutX + prefWidth) / TimelineController.pixelPerFrame).toInt()
        //微妙なズレを修正
//        layoutX = cObject.start * TimelineController.pixelPerFrame
//        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX
        cObject.onLayoutUpdate(mode)
    }

    fun onCaretChanged(frame: Int) {
        currentFrame = frame - cObject.start
        for (ps in properties)
            for (p in ps.property) {
                val pro = p.property
                when (pro) {
                    is MutableProperty -> {
                        //フレーム移動時にプレビュー用モードオフ
                        pro.temporaryMode = false
                        (p.node as CustomSlider).value = pro.value(currentFrame)

                        (p.node as CustomSlider).style = "-fx-background-color:" + when {
                            pro.keyFrames.size == 0 -> "#323232"
                            pro.isKeyFrame(currentFrame) != -1 -> "#FFFF00"
                            else -> "#9B5A00"
                        }
                    }
                }
            }
    }

    fun onScaleChanged() {
        layoutX = cObject.start * TimelineController.pixelPerFrame
        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX

        for (ps in properties)
            for (p in ps.property) {
                val pro = p.property
                val pane = p.pane
                if (pane != null && pro is MutableProperty) {
                    for ((i, v) in pane.children.withIndex()) {
                        v.layoutX = pro.keyFrames[i].frame * TimelineController.pixelPerFrame
                    }
                }
            }
        cObject.onScaleUpdate()
    }

    fun onDelete() {
        (parent as Pane).children.remove(this)
        Statics.project.Layer[cObject.layer].remove(cObject)
        println("ondelete")
    }

}