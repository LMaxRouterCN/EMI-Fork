package dev.emi.emi.jemi.impl.extras;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;

public class JemiPlaceable<T extends IPlaceable<T>> implements IPlaceable<T> {
	public int x = 0, y = 0;
	public int width, height;

	public JemiPlaceable(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public T setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return (T) this;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	// [长期记忆: 005] JEI 15.62 把 IPlaceable#setPosition(6 参:区域+对齐)从 default
	// 改为 abstract(15.20 中是 default,EMI 一直依赖它的默认实现),本类必须补真实现。
	// 语义:用自身 width/height 在给定区域内按水平/垂直对齐,算出绝对坐标后委托 setPosition(int, int)。
	// @Override 合法(15.20 的 IPlaceable 中该方法已存在为 default);返回 T 的擦除即 IPlaceable,
	// 恰好同时满足 15.62 raw IPlaceable 的描述符。子类(如 JemiTextWidget)需要协变返回时,
	// 重声明本方法即可让编译器自动生成桥方法,两个描述符并存。
	@Override
	public T setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		int x = areaX;
		if (horizontalAlignment == HorizontalAlignment.CENTER) {
			x += (areaWidth - width) / 2;
		} else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
			x += areaWidth - width;
		}
		int y = areaY;
		if (verticalAlignment == VerticalAlignment.CENTER) {
			y += (areaHeight - height) / 2;
		} else if (verticalAlignment == VerticalAlignment.BOTTOM) {
			y += areaHeight - height;
		}
		return setPosition(x, y);
	}
}
