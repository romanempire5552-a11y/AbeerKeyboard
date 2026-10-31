package com.abeer.keyboard

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Context
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.*
import android.text.InputType
import org.json.JSONArray
import kotlin.random.Random

class AbeerImeService : InputMethodService() {
    private lateinit var root: LinearLayout
    private lateinit var editor: EditText
    private var currentStyle = "Double"
    private val styles = listOf("Normal","Double","Triple","Alternating","Reverse","Spaced","Fancy","Bold","Bubble")
    private val prefs by lazy { getSharedPreferences("abeer_keyboard", Context.MODE_PRIVATE) }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8,8,8,8)
            setBackgroundColor(Color.rgb(29,32,40))
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        top.addView(label("ABEER KEYBOARD", 16f, Color.WHITE), LinearLayout.LayoutParams(0, dp(42), 1f))
        top.addView(action("⚙") { showOptions() }, LinearLayout.LayoutParams(dp(48), dp(42)))
        root.addView(top)

        editor = EditText(this).apply {
            hint = "Type here..."
            textSize = 18f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(12,4,12,4)
            setBackground(round(Color.rgb(55,60,72)))
        }
        root.addView(editor, LinearLayout.LayoutParams(-1, dp(48)))

        val stylesRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Double","Triple","Alternating","Reverse","Spaced").forEach { s ->
            stylesRow.addView(action(s) { currentStyle=s; Toast.makeText(this@AbeerImeService, "$s selected", Toast.LENGTH_SHORT).show() },
                LinearLayout.LayoutParams(0, dp(42), 1f))
        }
        root.addView(stylesRow)
        val extraRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Fancy","Bold","Bubble","Random","ABC").forEach { s ->
            extraRow.addView(action(s) {
                when(s) {
                    "Random" -> { currentStyle = styles.filter { it != "Normal" }.random(); insertText(transform(editor.text.toString(), currentStyle)); editor.text.clear() }
                    "ABC" -> { currentStyle="Normal"; insertText(editor.text.toString()); editor.text.clear() }
                    else -> { currentStyle=s; insertText(transform(editor.text.toString(), currentStyle)); editor.text.clear() }
                }
            }, LinearLayout.LayoutParams(0, dp(42), 1f))
        }
        root.addView(extraRow)
        val phraseRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        phraseRow.addView(action("Phrases") { showPhrases() }, LinearLayout.LayoutParams(0, dp(42), 1f))
        phraseRow.addView(action("Save phrase") { savePhrase() }, LinearLayout.LayoutParams(0, dp(42), 1f))
        phraseRow.addView(action("Custom style") { showCustomStyle() }, LinearLayout.LayoutParams(0, dp(42), 1f))
        root.addView(phraseRow)

        val keyRows = listOf("qwertyuiop","asdfghjkl","zxcvbnm")
        keyRows.forEach { rowText ->
            val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
            rowText.forEach { ch ->
                row.addView(action(ch.toString()) { editor.append(ch.toString()) },
                    LinearLayout.LayoutParams(0, dp(43), 1f).apply { setMargins(2,2,2,2) })
            }
            root.addView(row)
        }
        val bottom = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        bottom.addView(action("?123") { editor.append("123") }, LinearLayout.LayoutParams(dp(58),dp(44)))
        bottom.addView(action("space") { editor.append(" ") }, LinearLayout.LayoutParams(0,dp(44),1f))
        bottom.addView(action("⌫") { val t=editor.text; if(t.isNotEmpty()) t.delete(t.length-1,t.length) }, LinearLayout.LayoutParams(dp(52),dp(44)))
        bottom.addView(action("SEND ↵") { val value=transform(editor.text.toString(),currentStyle); insertText(value); editor.text.clear() }, LinearLayout.LayoutParams(dp(90),dp(44)))
        root.addView(bottom)
        return root
    }

    private fun transform(input: String, style: String): String {
        if (style == "Normal") return input
        if (style == "Reverse") return input.reversed()
        if (style == "Spaced") return input.toList().joinToString(" ")
        val map = mapOf('a' to 'ⓐ','b' to 'ⓑ','c' to 'ⓒ','d' to 'ⓓ','e' to 'ⓔ','f' to 'ⓕ','g' to 'ⓖ','h' to 'ⓗ','i' to 'ⓘ','j' to 'ⓙ','k' to 'ⓚ','l' to 'ⓛ','m' to 'ⓜ','n' to 'ⓝ','o' to 'ⓞ','p' to 'ⓟ','q' to 'ⓠ','r' to 'ⓡ','s' to 'ⓢ','t' to 'ⓣ','u' to 'ⓤ','v' to 'ⓥ','w' to 'ⓦ','x' to 'ⓧ','y' to 'ⓨ','z' to 'ⓩ')
        return when(style) {
            "Double" -> input.map { if(it.isLetter()) "$it$it" else it.toString() }.joinToString("")
            "Triple" -> input.map { if(it.isLetter()) "$it$it$it" else it.toString() }.joinToString("")
            "Alternating" -> input.mapIndexed { i,c -> if(c.isLetter()) if(i%2==0)c.lowercaseChar() else c.uppercaseChar() else c }.joinToString("")
            "Bubble" -> input.lowercase().map { map[it] ?: it }.joinToString("")
            "Custom" -> {
                val template = prefs.getString("custom", "{x}") ?: "{x}"
                input.map { template.replace("{x}", it.toString()) }.joinToString("")
            }
            "Bold" -> input.map { c -> if(c in 'a'..'z') (0x1D41A + (c-'a')).toChar() else if(c in 'A'..'Z') (0x1D400 + (c-'A')).toChar() else c }.joinToString("")
            "Fancy" -> input.map { c -> if(c in 'a'..'z') (0x1D4B6 + (c-'a')).toChar() else c }.joinToString("")
            else -> input
        }
    }

    private fun insertText(s: String) { if(s.isNotEmpty()) currentInputConnection?.commitText(s,1) }
    private fun showOptions() {
        AlertDialogBuilder("Keyboard settings", "Enable Abeer Keyboard in Android Settings → System/Additional settings → Keyboard & input method, then select it in WePlay.")
    }
    private fun showPhrases() {
        val phrases = loadPhrases()
        if(phrases.isEmpty()) { AlertDialogBuilder("No saved phrases", "Tap Save phrase after typing a phrase."); return }
        android.app.AlertDialog.Builder(this).setTitle("Saved phrases").setItems(phrases.toTypedArray()) { _, which -> insertText(phrases[which]) }.setNegativeButton("Close",null).show()
    }
    private fun savePhrase() {
        val value=editor.text.toString().trim()
        if(value.isEmpty()) { Toast.makeText(this,"Type a phrase first",Toast.LENGTH_SHORT).show(); return }
        val list=loadPhrases().toMutableList(); list.add(value)
        prefs.edit().putString("phrases", JSONArray(list).toString()).apply()
        editor.text.clear(); Toast.makeText(this,"Phrase saved",Toast.LENGTH_SHORT).show()
    }
    private fun loadPhrases(): List<String> = try { val a=JSONArray(prefs.getString("phrases","[]")); (0 until a.length()).map { a.getString(it) } } catch (_:Exception) { emptyList() }
    private fun showCustomStyle() {
        val box=EditText(this).apply { hint="e.g. {x}{x}"; setSingleLine(true) }
        android.app.AlertDialog.Builder(this).setTitle("Custom template").setMessage("Use {x} for each letter. Example: {x}{x} doubles every letter.").setView(box)
            .setPositiveButton("Apply") { _,_ -> val template=box.text.toString(); if(template.contains("{x}")) { currentStyle="Custom"; prefs.edit().putString("custom",template).apply(); Toast.makeText(this,"Custom template saved. Use SEND.",Toast.LENGTH_SHORT).show() } }
            .setNegativeButton("Cancel",null).show()
    }
    private fun AlertDialogBuilder(title:String, message:String) { android.app.AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show() }
    private fun label(t:String,size:Float,color:Int)=TextView(this).apply { text=t; textSize=size; setTextColor(color); gravity=android.view.Gravity.CENTER_VERTICAL; setTypeface(null,Typeface.BOLD) }
    private fun action(t:String, click:()->Unit)=TextView(this).apply {
        text=t; textSize=12f; gravity=android.view.Gravity.CENTER; setTextColor(Color.WHITE); setPadding(3,3,3,3)
        background=round(Color.rgb(64,72,88)); setOnClickListener { click() }
    }
    private fun round(color:Int)=GradientDrawable().apply { setColor(color); cornerRadius=dp(7).toFloat() }
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
}
