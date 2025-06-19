package com.co.mywordle

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // Game constants
    private val MAX_ATTEMPTS = 6
    private val WORD_LENGTH = 5
    private val ABSENT_COLOR = Color.parseColor("#787C7E")    // Dark gray
    private val PRESENT_COLOR = Color.parseColor("#C9B458")   // Yellow
    private val CORRECT_COLOR = Color.parseColor("#6AAA64")   // Green
    private val DEFAULT_COLOR = Color.parseColor("#FFFFFF")   // White
    private val KEYBOARD_DEFAULT_COLOR = Color.parseColor("#E0E0E0") // Light grey
    private val TEXT_DEFAULT_COLOR = Color.BLACK
    private val TEXT_COLORED_COLOR = Color.WHITE

    // Game state
    private lateinit var targetWord: String
    private var currentRow = 1
    private var currentCol = 1
    private var gameOver = false

    // UI components
    private lateinit var letterCells: Array<Array<TextView>>
    private val keyboardButtons = HashMap<Char, Button>()
    private lateinit var enterButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize word list
        AppData.initWords()

        // Initialize the game
        initializeUI()
        selectRandomWord()
        setupKeyboard()
        resetGame()
    }

    private fun initializeUI() {
        // Initialize letter grid with explicit IDs to avoid runtime lookup errors
        letterCells = arrayOf(
            arrayOf(
                findViewById<TextView>(R.id.inputLetter_11_id),
                findViewById<TextView>(R.id.inputLetter_12_id),
                findViewById<TextView>(R.id.inputLetter_13_id),
                findViewById<TextView>(R.id.inputLetter_14_id),
                findViewById<TextView>(R.id.inputLetter_15_id)
            ),
            arrayOf(
                findViewById<TextView>(R.id.text_Letter21_id),
                findViewById<TextView>(R.id.text_Letter22_id),
                findViewById<TextView>(R.id.text_Letter23_id),
                findViewById<TextView>(R.id.text_Letter24_id),
                findViewById<TextView>(R.id.text_Letter25_id)
            ),
            arrayOf(
                findViewById<TextView>(R.id.inputLetter31_id),
                findViewById<TextView>(R.id.inputLetter32_id),
                findViewById<TextView>(R.id.inputLetter33_id),
                findViewById<TextView>(R.id.inputLetter34_id),
                findViewById<TextView>(R.id.inputLetter35_id)
            ),
            arrayOf(
                findViewById<TextView>(R.id.inputLetter41_id),
                findViewById<TextView>(R.id.inputLetter42_id),
                findViewById<TextView>(R.id.inputLetter43_id),
                findViewById<TextView>(R.id.inputLetter44_id),
                findViewById<TextView>(R.id.inputLetter45_id)
            ),
            arrayOf(
                findViewById<TextView>(R.id.inputLetter51_id),
                findViewById<TextView>(R.id.inputLetter52_id),
                findViewById<TextView>(R.id.inputLetter53_id),
                findViewById<TextView>(R.id.inputLetter54_id),
                findViewById<TextView>(R.id.inputLetter55_id)
            ),
            arrayOf(
                findViewById<TextView>(R.id.inputLetter61_id),
                findViewById<TextView>(R.id.inputLetter62_id),
                findViewById<TextView>(R.id.inputLetter63_id),
                findViewById<TextView>(R.id.inputLetter64_id),
                findViewById<TextView>(R.id.inputLetter65_id)
            )
        )

        // Disable focus & soft keyboard, and clear text
        letterCells.flatten().forEach { view ->
            view.apply {
                isFocusable = false
                isFocusableInTouchMode = false
                isClickable = false
                setTextIsSelectable(false)
                if (this is android.widget.EditText) {
                    showSoftInputOnFocus = false
                }
                text = ""
                setTextColor(TEXT_DEFAULT_COLOR)
                setBackgroundResource(R.drawable.box_border)
            }
        }

        // Find and initialize keyboard buttons
        val keyboardChars = "QWERTYUIOPASDFGHJKLZXCVBNM"
        keyboardChars.forEach { ch ->
            val resourceId = resources.getIdentifier("${ch.lowercaseChar()}Button_id", "id", packageName)
            val buttonNullable = if (resourceId != 0) findViewById<Button>(resourceId) else null
            buttonNullable?.let { btn ->
                keyboardButtons[ch] = btn
                btn.setOnClickListener { onLetterClick(ch) }
            }
        }

        // Setup Enter and Delete buttons
        enterButton = findViewById(R.id.enterButton_id)
        enterButton.setOnClickListener { onEnterClick() }

        deleteButton = findViewById(R.id.deleteButton_id)
        deleteButton.setOnClickListener { onDeleteClick() }
    }

    private fun selectRandomWord() {
        // Select a random word from the list
        targetWord = AppData.words.random().word
    }

    private fun setupKeyboard() {
        // Reset keyboard colors and text colors
        keyboardButtons.values.forEach { button ->
            button.setBackgroundColor(KEYBOARD_DEFAULT_COLOR)
            button.setTextColor(TEXT_DEFAULT_COLOR)
            button.tag = 0 // default state
        }
    }

    private fun resetGame() {
        // Reset game state
        currentRow = 1
        currentCol = 1
        gameOver = false

        // Clear all letter cells
        for (row in 0 until MAX_ATTEMPTS) {
            for (col in 0 until WORD_LENGTH) {
                letterCells[row][col].text = ""
                letterCells[row][col].setBackgroundResource(R.drawable.box_border)
            }
        }

        // Reset keyboard
        setupKeyboard()

        // Select a new random word
        selectRandomWord()
    }

    private fun onLetterClick(letter: Char) {
        if (gameOver || currentRow > MAX_ATTEMPTS || currentCol > WORD_LENGTH) return

        // Add letter to current position
        if (currentCol <= WORD_LENGTH) {
            val cell = letterCells[currentRow - 1][currentCol - 1]
            cell.text = letter.toString()
            cell.setTextColor(TEXT_DEFAULT_COLOR)
            currentCol++
        }
    }

    private fun onDeleteClick() {
        if (gameOver || currentRow > MAX_ATTEMPTS) return

        // Remove letter from previous position
        if (currentCol > 1) {
            currentCol--
            val cell = letterCells[currentRow - 1][currentCol - 1]
            cell.text = ""
            cell.setTextColor(TEXT_DEFAULT_COLOR)
        }
    }

    private fun onEnterClick() {
        if (gameOver || currentRow > MAX_ATTEMPTS) return

        // Check if row is complete
        if (currentCol <= WORD_LENGTH) {
            Toast.makeText(this, "Complete the word first", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect current word
        val guessWord = (0 until WORD_LENGTH).map { col ->
            letterCells[currentRow - 1][col].text.toString()
        }.joinToString("")

        // Validate word is in the list
        if (!isValidWord(guessWord)) {
            Toast.makeText(this, "Not in word list", Toast.LENGTH_SHORT).show()
            return
        }

        // Check letters and update colors
        checkWord(guessWord)

        // Check if player won
        if (guessWord == targetWord) {
            gameOver = true
            showGameEndDialog(true)
            return
        }

        // Move to next row
        currentRow++
        currentCol = 1

        // Check if player lost
        if (currentRow > MAX_ATTEMPTS) {
            gameOver = true
            showGameEndDialog(false)
        }
    }

    private fun isValidWord(word: String): Boolean {
        return AppData.words.any { it.word == word }
    }

    private fun checkWord(guessWord: String) {
        val targetCharCount = mutableMapOf<Char, Int>()
        
        // Count characters in target word
        targetWord.forEach { char ->
            targetCharCount[char] = (targetCharCount[char] ?: 0) + 1
        }
        
        // First pass: Mark correct letters
        val letterStates = Array(WORD_LENGTH) { -1 } // -1: unknown, 0: absent, 1: present, 2: correct
        
        for (i in 0 until WORD_LENGTH) {
            val guessChar = guessWord[i]
            
            if (guessChar == targetWord[i]) {
                letterStates[i] = 2 // correct position
                targetCharCount[guessChar] = (targetCharCount[guessChar] ?: 0) - 1
            }
        }
        
        // Second pass: Mark present and absent letters
        for (i in 0 until WORD_LENGTH) {
            if (letterStates[i] == 2) continue // Skip already marked correct
            
            val guessChar = guessWord[i]
            val count = targetCharCount[guessChar] ?: 0
            
            if (count > 0) {
                letterStates[i] = 1 // present but wrong position
                targetCharCount[guessChar] = count - 1
            } else {
                letterStates[i] = 0 // absent
            }
        }
        
        // Update UI based on letter states
        for (i in 0 until WORD_LENGTH) {
            val cell = letterCells[currentRow - 1][i]
            val guessChar = guessWord[i]
            val keyboardButton = keyboardButtons[guessChar]
            
            val (bgColor, keyStateValue) = when (letterStates[i]) {
                0 -> ABSENT_COLOR to 1
                1 -> PRESENT_COLOR to 2
                2 -> CORRECT_COLOR to 3
                else -> DEFAULT_COLOR to 0
            }

            // Apply to grid cell
            cell.setBackgroundColor(bgColor)
            if (letterStates[i] != -1) {
                cell.setTextColor(TEXT_COLORED_COLOR)
            }

            // Apply to keyboard key with precedence
            keyboardButton?.let { btn ->
                val currentState = (btn.tag as? Int) ?: 0
                if (keyStateValue > currentState) {
                    btn.tag = keyStateValue
                    btn.setBackgroundColor(bgColor)
                    btn.setTextColor(TEXT_COLORED_COLOR)
                }
            }
        }
    }

    private fun showGameEndDialog(isWin: Boolean) {
        val message = if (isWin) "Congratulations! You guessed the word!" else "Game over! The word was $targetWord"
        
        AlertDialog.Builder(this)
            .setTitle(if (isWin) "You Win!" else "You Lose")
            .setMessage(message)
            .setPositiveButton("Play Again") { dialog, _ ->
                resetGame()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}