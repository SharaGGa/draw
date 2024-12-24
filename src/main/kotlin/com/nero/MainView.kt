package com.nero

import com.nero.draw.DrawPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.imageio.ImageIO
import javax.swing.*
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import javax.swing.border.EmptyBorder


class MainView : JFrame(appName) {
    private val drawPanel: DrawPanel
    private var currentColor = Color.BLACK
    private var brushSize = 5
    private var isEraser = false
    private var isFilling = false
    private var shapeToDraw: String? = null

    private fun String.loadIcon(): ImageIcon? {
        val url = this@MainView.javaClass.getResource(this)
        return url?.let { ImageIcon(it) }
    }

    init {

        UIManager.setLookAndFeel(FlatMacDarkLaf())

        System.setProperty("flatlaf.flatlaf.useJetBrainsCustomDecorations", "true")
//        System.setProperty("flatlaf.menuBarEmbedded", "false")

        val appIcon = "/icon.png".loadIcon()
        if (appIcon != null) {
            iconImage = appIcon.image
        } else {
            println("иконка не найдена")
        }

        val panel = JPanel()
        val widthField = JTextField("800", 5)
        val heightField = JTextField("600", 5)

        panel.add(JLabel("Ширина:"))
        panel.add(widthField)
        panel.add(JLabel("Высота:"))
        panel.add(heightField)

        val result = JOptionPane.showConfirmDialog(this, panel, "Введите размер холста", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)

        val width = if (result == JOptionPane.OK_OPTION) {
            widthField.text.toIntOrNull() ?: 800
        } else {
            800
        }

        val height = if (result == JOptionPane.OK_OPTION) {
            heightField.text.toIntOrNull() ?: 600
        } else {
            600
        }

        val menuBar = JMenuBar()
        val fileMenu = JMenu("Файл")
        val saveItem = JMenuItem("Сохранить")
        val openItem = JMenuItem("Открыть")
        fileMenu.add(saveItem)
        fileMenu.add(openItem)
        menuBar.add(fileMenu)
        jMenuBar = menuBar
        ////////
        val toolPanel = JPanel()
        toolPanel.layout = GridLayout(2, 5, 5, 5) // Добавляем отступы в GridLayout
        toolPanel.border = EmptyBorder(10,10,10,10) // Добавляем отступы по краям панели
        ////////
        val colorButton = JButton("Цвет")
        val brushSlider = JSlider(JSlider.HORIZONTAL, 1, 20, 5)
        val eraserButton = JToggleButton("Ластик")
        val fillButton = JButton("Заливка")
        val lineButton = JButton("Прямая")
        val rectangleButton = JButton("Прямоугольник")
        val circleButton = JButton("Круг")
        val ellipseButton = JButton("Эллипс")
        val brushButton = JButton("Кисть")
        ////////
        val buttonBorder = EmptyBorder(5, 5, 5, 5)
        colorButton.border = buttonBorder
        eraserButton.border = buttonBorder
        fillButton.border = buttonBorder
        lineButton.border = buttonBorder
        rectangleButton.border = buttonBorder
        circleButton.border = buttonBorder
        ellipseButton.border = buttonBorder
        brushButton.border = buttonBorder
        ////////
        toolPanel.add(colorButton)
        toolPanel.add(brushSlider)
        toolPanel.add(eraserButton)
        toolPanel.add(fillButton)
        toolPanel.add(lineButton)
        toolPanel.add(rectangleButton)
        toolPanel.add(circleButton)
        toolPanel.add(ellipseButton)
        toolPanel.add(brushButton)
        ////////
        drawPanel = DrawPanel(width, height)
        drawPanel.border = EmptyBorder(10,10,10,10)
        drawPanel.background = UIManager.getColor("Panel.background")
        ////////
        add(toolPanel, BorderLayout.NORTH)
        add(drawPanel, BorderLayout.CENTER)
        ////////
        colorButton.addActionListener {
            val color = JColorChooser.showDialog(this, "Выбор цвета", currentColor)
            if (color != null) {
                currentColor = color
                isEraser = false
                isFilling = false
                eraserButton.isSelected = false
                drawPanel.setCurrentColor(currentColor)
                drawPanel.setIsEraser(isEraser)
                drawPanel.setIsFilling(isFilling)
            }
        }

        brushSlider.addChangeListener {
            brushSize = brushSlider.value
            drawPanel.updateBrushSize(brushSize)
        }

        eraserButton.addActionListener {
            isEraser = eraserButton.isSelected
            isFilling = false
            shapeToDraw = null
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        fillButton.addActionListener {
            isFilling = true
            isEraser = false
            eraserButton.isSelected = false
            shapeToDraw = null
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        lineButton.addActionListener {
            shapeToDraw = "line"
            isFilling = false
            isEraser = false
            eraserButton.isSelected = false
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        rectangleButton.addActionListener {
            shapeToDraw = "rectangle"
            isFilling = false
            isEraser = false
            eraserButton.isSelected = false
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        circleButton.addActionListener {
            shapeToDraw = "circle"
            isFilling = false
//            isEraser = false
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        ellipseButton.addActionListener {
            shapeToDraw = "ellipse"
            isFilling = false
            isEraser = false
            eraserButton.isSelected = false
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        brushButton.addActionListener {
            shapeToDraw = null
            isFilling = false
            isEraser = false
            eraserButton.isSelected = false
            drawPanel.setIsEraser(isEraser)
            drawPanel.setIsFilling(isFilling)
            drawPanel.setShapeToDraw(shapeToDraw)
        }

        saveItem.addActionListener {
            val fileChooser = JFileChooser()
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                ImageIO.write(drawPanel.getImage(), "png", file)
            }
        }

        openItem.addActionListener {
            val fileChooser = JFileChooser()
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val image = ImageIO.read(file)
                drawPanel.setImage(image)
            }
        }
        ////////
        pack()
        size = Dimension(800, 700)
        setLocationRelativeTo(null)
        isVisible = true
        defaultCloseOperation = EXIT_ON_CLOSE
        SwingUtilities.updateComponentTreeUI(this)
    }
}