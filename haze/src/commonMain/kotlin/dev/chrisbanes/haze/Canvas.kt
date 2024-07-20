// Copyright 2024, Christopher Banes and the Haze project contributors
// SPDX-License-Identifier: Apache-2.0

package dev.chrisbanes.haze

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer

internal fun Canvas.clipShape(
  shape: Shape,
  bounds: Rect,
  clipOp: ClipOp = ClipOp.Intersect,
  path: () -> Path,
) {
  if (shape == RectangleShape) {
    clipRect(bounds, clipOp)
  } else {
    pathPool.usePath { tmpPath ->
      tmpPath.addPath(path(), bounds.topLeft)
      clipPath(tmpPath, clipOp)
    }
  }
}

internal fun DrawScope.clipShape(
  shape: Shape,
  size: Size,
  offset: Offset = Offset.Unspecified,
  clipOp: ClipOp = ClipOp.Intersect,
  path: () -> Path,
  block: DrawScope.() -> Unit,
) {
  if (shape == RectangleShape) {
    clipRect(
      left = offset.x,
      top = offset.y,
      right = offset.x + size.width,
      bottom = offset.y + size.height,
      clipOp = clipOp,
      block = block,
    )
  } else {
    if (offset.isUnspecified || offset == Offset.Zero) {
      clipPath(path(), clipOp, block)
    } else {
      pathPool.usePath { tmpPath ->
        tmpPath.addPath(path(), offset)
        clipPath(tmpPath, clipOp, block)
      }
    }
  }
}

internal inline fun GraphicsContext.useGraphicsLayer(block: (GraphicsLayer) -> Unit) {
  val layer = createGraphicsLayer()
  try {
    block(layer)
  } finally {
    releaseGraphicsLayer(layer)
  }
}

inline fun DrawScope.translate(
  offset: Offset,
  block: DrawScope.() -> Unit,
) = translate(offset.x, offset.y, block)
