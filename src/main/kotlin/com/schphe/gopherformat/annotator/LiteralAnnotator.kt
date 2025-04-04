package com.schphe.gopherformat.annotator

import com.schphe.gopherformat.FormatData
import com.schphe.gopherformat.FormatHelper
import com.goide.GoLanguage
import com.goide.highlighting.GoSyntaxHighlightingColors
import com.goide.psi.GoCallExpr
import com.goide.psi.GoStringLiteral
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import java.awt.Font

class LiteralAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!FormatHelper.isStringLiteral(element)) return
        var mixed = false

        FormatHelper.iterateTags(element.text, element.textRange.startOffset) { rangeOut, _, key, offset ->
            !setColor(holder, key, rangeOut.shiftRight(offset))
                    && !setFont(holder, key, rangeOut.shiftRight(offset), mixed).let {
                if (it) mixed = true
                it
            }
        }

        if (element.language is GoLanguage) return

        FormatHelper.iterateSymbols(element.text, element.textRange.startOffset) { range ->
            FormatHelper.highlightPlaceholder(holder, range)
        }
    }

    private fun setColor(holder: AnnotationHolder, key: String, range: TextRange): Boolean {
        FormatData.ColorCode[key]?.let {
            FormatHelper.createAnnotation(holder, range, it)
            return true
        }
        return false
    }

    private fun setFont(holder: AnnotationHolder, key: String, range: TextRange, mixed: Boolean): Boolean {
        FormatData.FontCode[key]?.let {
            FormatHelper.createAnnotation(holder, range, fontType = if(mixed) {
                Font.BOLD + Font.ITALIC
            } else {
                it
            })
            return true
        }
        return false
    }
}
