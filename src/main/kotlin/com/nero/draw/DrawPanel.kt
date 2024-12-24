package com.nero.draw

import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.swing.JPanel
import kotlin.math.abs

class DrawPanel(width: Int, height: Int) : JPanel() {
    private var image: BufferedImage? = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private var g2d: Graphics2D? = null
    private var lastPoint: Point? = null
    private var scale = 1.0
    private var offsetX = 0
    private var offsetY = 0
    private var dragging = false
    private var dragStartPoint: Point? = null
    private val transform = AffineTransform()
    private var drawBuffer: BufferedImage? = null
    private var brushSize = 5
    private var startPoint: Point? = null
    private var endPoint: Point? = null
    private var shapeToDraw: String? = null
    private var currentColor = Color.BLACK
    private var isEraser = false
    private var isFilling = false

    init {
        background = Color.WHITE
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        g2d = image!!.createGraphics()
        g2d!!.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        updateTransform()
        ////////
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON2) {
                    dragging = true
                    dragStartPoint = e.point
                } else {
                    lastPoint = e.point
                    startPoint = e.point
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON2) {
                    dragging = false
                } else {
                    lastPoint = null
                    endPoint?.let { drawFinalShape(it) }
                    startPoint = null
                    endPoint = null
                }
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (dragging) {
                    offsetX += e.x - (dragStartPoint?.x ?: 0)
                    offsetY += e.y - (dragStartPoint?.y ?: 0)
                    dragStartPoint = e.point
                    updateTransform()
                    repaint()
                } else {
                    val currentPoint = e.point
                    if (lastPoint != null) {
                        if (image == null) {
                            image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                            g2d = image!!.createGraphics()
                            g2d!!.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        }

                        if (isFilling) {
                            floodFill(transformX(currentPoint.x), transformY(currentPoint.y))
                        } else if (shapeToDraw != null) {
                            endPoint = currentPoint
                            repaint()
                        } else {
                            g2d!!.stroke = BasicStroke(brushSize.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

                            if (isEraser) {
                                g2d!!.composite = AlphaComposite.getInstance(AlphaComposite.SRC, 0f)
                            } else {
                                g2d!!.composite = AlphaComposite.getInstance(AlphaComposite.SRC, 1f)
                                g2d!!.color = currentColor
                            }

                            g2d!!.drawLine(
                                ((lastPoint!!.x - offsetX) / scale).toInt(),
                                ((lastPoint!!.y - offsetY) / scale).toInt(),
                                ((currentPoint.x - offsetX) / scale).toInt(),
                                ((currentPoint.y - offsetY) / scale).toInt()
                            )
                            if (isEraser) {
                                g2d!!.fillRect(
                                    ((currentPoint.x - offsetX) / scale).toInt() - brushSize / 2,
                                    ((currentPoint.y - offsetY) / scale).toInt() - brushSize / 2,
                                    brushSize,
                                    brushSize
                                )
                            }
                            repaint()
                        }
                    }
                    lastPoint = currentPoint
                }
            }
        })

        addMouseWheelListener { e ->
            if (e.modifiersEx and InputEvent.CTRL_DOWN_MASK != 0) {
                val oldScale = scale
                scale *= if (e.wheelRotation < 0) 1.1 else 0.9

                val mouseX = e.x
                val mouseY = e.y
                offsetX = (mouseX - (mouseX - offsetX) * scale / oldScale).toInt()
                offsetY = (mouseY - (mouseY - offsetY) * scale / oldScale).toInt()

                updateTransform()

                repaint()
            }
        }
    }
    ////////
    private fun transformX(x: Int): Int = ((x - offsetX) / scale).toInt()
    private fun transformY(y: Int): Int = ((y - offsetY) / scale).toInt()

    private fun updateTransform() {
        transform.setToIdentity()
        transform.translate(offsetX.toDouble(), offsetY.toDouble())
        transform.scale(scale, scale)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        if (image != null) {
            g2d.drawImage(image, transform, null)
            g2d.color = Color.BLACK
            g2d.drawRect(offsetX, offsetY, (image!!.width * scale).toInt(), (image!!.height * scale).toInt())
        }

        if (startPoint != null && endPoint != null) {
            g2d.stroke = BasicStroke(brushSize * scale.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2d.color = currentColor

            when (shapeToDraw) {
                "line" -> g2d.drawLine(startPoint!!.x, startPoint!!.y, endPoint!!.x, endPoint!!.y)
                "rectangle" -> g2d.drawRect(
                    minOf(startPoint!!.x, endPoint!!.x),
                    minOf(startPoint!!.y, endPoint!!.y),
                    abs(endPoint!!.x - startPoint!!.x),
                    abs(endPoint!!.y - startPoint!!.y)
                )
                "circle" -> g2d.drawOval(
                    minOf(startPoint!!.x, endPoint!!.x),
                    minOf(startPoint!!.y, endPoint!!.y),
                    abs(endPoint!!.x - startPoint!!.x),
                    abs(endPoint!!.x - startPoint!!.x)
                )
                "ellipse" -> g2d.drawOval(
                    minOf(startPoint!!.x, endPoint!!.x),
                    minOf(startPoint!!.y, endPoint!!.y),
                    abs(endPoint!!.x - startPoint!!.x),
                    abs(endPoint!!.y - startPoint!!.y)
                )
            }
        }

        g2d.dispose()
    }
    ////////
    private fun floodFill(x: Int, y: Int) {
        if (x < 0 || x >= image!!.width || y < 0 || y >= image!!.height) return
        val targetColor = image!!.getRGB(x, y)
        if (targetColor == currentColor.rgb) return

        val stack = mutableListOf<Point>()
        stack.add(Point(x, y))

        while (stack.isNotEmpty()) {
            val p = stack.removeAt(stack.size - 1)
            val px = p.x
            val py = p.y

            if (px < 0 || px >= image!!.width || py < 0 || py >= image!!.height) continue

            if (image!!.getRGB(px, py) == targetColor) {
                image!!.setRGB(px, py, currentColor.rgb)

                stack.add(Point(px + 1, py))
                stack.add(Point(px - 1, py))
                stack.add(Point(px, py + 1))
                stack.add(Point(px, py - 1))
            }
        }
        repaint()
    }

    private fun drawFinalShape(point: Point) {
        val g = image!!.createGraphics()
        g.color = currentColor
        g.stroke = BasicStroke(brushSize.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        val adjustedStartX = transformX(startPoint!!.x)
        val adjustedStartY = transformY(startPoint!!.y)
        val adjustedEndX = transformX(point.x)
        val adjustedEndY = transformY(point.y)

        when (shapeToDraw) {
            "line" -> g.drawLine(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY)
            "rectangle" -> g.drawRect(
                minOf(adjustedStartX, adjustedEndX),
                minOf(adjustedStartY, adjustedEndY),
                abs(adjustedEndX - adjustedStartX),
                abs(adjustedEndY - adjustedStartY)
            )
            "circle" -> g.drawOval(
                minOf(adjustedStartX, adjustedEndX),
                minOf(adjustedStartY, adjustedEndY),
                abs(adjustedEndX - adjustedStartX),
                abs(adjustedEndX - adjustedStartX)
            )
            "ellipse" -> g.drawOval(
                minOf(adjustedStartX, adjustedEndX),
                minOf(adjustedStartY, adjustedEndY),
                abs(adjustedEndX - adjustedStartX),
                abs(adjustedEndY - adjustedStartY)
            )
        }
        g.dispose()
        repaint()
    }

    fun updateBrushSize(size: Int) {
        brushSize = size
    }

    fun getImage(): BufferedImage {
        if (image == null) {
            image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        }
        return image!!
    }

    override fun removeNotify() {
        super.removeNotify()
        g2d?.dispose()
        g2d = null
        image?.flush()
        image = null
        drawBuffer?.flush()
        drawBuffer = null
    }

    fun setImage(newImage: BufferedImage) {
        g2d?.dispose()
        image?.flush()

        image = BufferedImage(newImage.width, newImage.height, BufferedImage.TYPE_INT_ARGB)
        val g = image!!.createGraphics()
        g.drawImage(newImage, 0, 0, null)
        g.dispose()


        g2d = image?.createGraphics()
        g2d!!.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawBuffer?.flush()
        drawBuffer = BufferedImage(image!!.width, image!!.height, BufferedImage.TYPE_INT_ARGB)
        updateTransform()


        scale = 1.0
        offsetX = 0
        offsetY = 0
        updateTransform()

        repaint()
    }

    fun setCurrentColor(color: Color) {
        currentColor = color
    }

    fun setIsEraser(eraser: Boolean){
        isEraser = eraser
    }
    fun setIsFilling(filling: Boolean){
        isFilling = filling
    }
    fun setShapeToDraw(shape: String?){
        shapeToDraw = shape
    }
}