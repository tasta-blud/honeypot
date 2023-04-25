package honeypot.css

import nl.astraeus.css.properties.*
import nl.astraeus.css.style
import nl.astraeus.css.style.ConditionalStyle
import nl.astraeus.css.style.Style
import nl.astraeus.css.style.cls
import nl.astraeus.css.style.id

object Stylesheets {
    private val backgroundColor = Color("#000000")
    private val bodyColor = Color("#cccccc")
    private val secondaryColor = Color("#333333")
    private val dangerColor = Color("#ff0000")
    private val successColor = Color("#008000")
    private val tertiaryColor = Color("#808080")
    private val highlightColor = Color("#ffff00")
    private val hoverColor = Color("#ccccff")
    private val collapseColor = Color("#660000")

    val stylesheet: String = style {
        cssCommon()
        select(cls("tools")) {
            cssTools()
        }
        select(id("content")) {
            cssContent()
        }
    }.generateCss()

    private fun ConditionalStyle.cssCommon() {
        select("body") {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls("label")) {
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select("hr") {
            borders(secondaryColor)
        }
    }

    private fun Style.cssContent() {
        select(cls("message")) {
            and(cls("message-error")) {
                color(dangerColor)
            }
            and(cls("message-success")) {
                color(successColor)
            }
        }
        select(cls("clickable")) {
            select("label") {
                cursor("pointer")
            }
            cursor("pointer")
        }
        select(cls("label")) {
            and(cls("highlight")) {
                color(highlightColor)
            }
        }
        select(cls("cell-label")) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
            verticalAlign(VerticalAlign.top)
        }
        select(cls("cell-data")) {
            borders(Color.transparent)
            and(cls("single")) {
                whiteSpace(WhiteSpace.nowrap)
            }
        }
        select(cls("editor")) {
            minWidth(95.prc)
            maxWidth(35.em)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            and(cls("editor-boolean")) {
                minWidth(3.em)
                maxWidth(3.em)
            }
            and(cls("changed")) {
                borderColor(highlightColor)
            }
        }
        select(cls("switch")) {
            val switchHeight = 17.px
            val switchWidth = 30.px
            val sliderSize = 13.px
            val sliderPos = 2.px
            val colorOn = Color.mediumBlue
            val colorOff = Color("#333333")
            val color = Color.white
            val transition = ".4s"
            display(Display.inlineBlock)
            height(switchHeight)
            position(Position.relative)
            width(switchWidth)
            select("input") {
                display(Display.none)
            }
            select(cls("slider")) {
                backgroundColor(colorOff)
                bottom(0.px)
                cursor("pointer")
                left(0.px)
                position(Position.absolute)
                right(0.px)
                top(0.px)
                transition(transition)
                and((":before")) {
                    backgroundColor(color)
                    bottom(sliderPos)
                    content(Content("''"))
                    height(sliderSize)
                    left(sliderPos)
                    position(Position.absolute)
                    transition(transition)
                    width(sliderSize)
                }
                and(cls("round")) {
                    borderRadius(switchHeight)
                    and(":before") {
                        borderRadius(50.prc)
                    }
                }
            }
            select("input:checked + .slider") {
                backgroundColor(colorOn)
                and(":before") {
//                    transform(Transform.translateX(26.0))
                    transform(Transform("translateX($sliderSize)"))
                }
            }
        }
        select("input[type=\"checkbox\"].editor-lock") {
            display(Display.none)
            and(" + label:before") { content(Content.url("css/lockopen.svg")) }
            and(":checked + label:before") { content(Content.url("css/lock.svg")) }
        }
        select(cls("object-empty")) {
            color(tertiaryColor)
        }
        select(cls("grid")) {
            width(100.prc)
            select(cls("row")) {
                borderCollapse(BorderCollapse.collapse)
                bordersHorizontal(secondaryColor)
                hover {
                    bordersHorizontal(hoverColor)
                }
                select(cls("cell")) {
                    bordersHorizontal(Color.inherit)
                    hover {
                        bordersHorizontal(Color.inherit)
                    }
                    and(cls("cell-label")) {
                        bordersHorizontal(Color.inherit)
                        bordersLeft(Color.inherit)
                        hover {
                            bordersHorizontal(Color.inherit)
                            bordersLeft(Color.inherit)
                        }
                    }
                    and(cls("cell-data")) {
                        bordersHorizontal(Color.inherit)
                        bordersRight(Color.inherit)
                        hover {
                            bordersHorizontal(Color.inherit)
                            bordersRight(Color.inherit)
                        }
                    }
                }
            }
            select(cls("highlight")) {
                select("input") {
                    color(highlightColor)
                }
                select("textarea") {
                    color(highlightColor)
                }
            }
        }
        select(cls("hidden")) {
            display(Display.none)
        }
        select(cls("collapsed")) {
            borders(collapseColor)
            width(55.em)
            select("*") {
                display(Display.none)
            }
        }
    }

    private fun Style.cssTools() {
        width(100.prc)
        select(cls("cell-center")) {
            textAlign(TextAlign.center)
        }
        select("a") {
            color(Color.inherit)
            textDecoration("none")
        }
        select(cls("caption")) {
            fontSize(FontSize.large)
            margin(0.px)
        }
        select(cls("cell-label")) {
            textAlign(TextAlign.right)
            fontWeight(FontWeight.normal)
        }
        select(cls("editor")) {
            minWidth(95.prc)
            maxWidth(35.em)
            textAlign(TextAlign.left)
            backgroundColor(backgroundColor)
            color(bodyColor)
            and(cls("editor-min")) {
                width(8.em)
            }
        }
        select(cls("button")) {
            textAlign(TextAlign.center)
            backgroundColor(backgroundColor)
            color(bodyColor)
        }
        select(cls("small")) {
            fontSize(FontSize.small)
            color(secondaryColor)
        }
    }
}
