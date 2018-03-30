package annotation

/**
 * アノテーション定義
 */

/**
 * CitrusObjectのプロパティに付与するアノテーション
 * 付与されたプロパティは編集画面から制御できるようになる
 * @param displayName 実際に表示されるパラメーター名
 * @param index 表示される順番
 */
@Target(AnnotationTarget.PROPERTY)
annotation class CProperty(val displayName : String,val index : Int)

/**
 * CitrusObject自体に付与するアノテーション
 * そのオブジェクトの表示名を定義する意図もある
 * @param name 表示名
 */
@Target(AnnotationTarget.CLASS)
annotation class CObject(val name:String)