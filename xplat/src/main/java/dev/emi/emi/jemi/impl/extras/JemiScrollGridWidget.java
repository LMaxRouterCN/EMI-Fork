package dev.emi.emi.jemi.impl.extras;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;

public class JemiScrollGridWidget implements IScrollGridWidget {
	private static final int SCROLL_BAR_WIDTH = 18;
	public List<IRecipeSlotDrawable> slots;
	public int x, y;
	public int gridWidth, gridHeight;
	public int width, height;

	public JemiScrollGridWidget(List<IRecipeSlotDrawable> slots, int x, int y, int gridWidth, int gridHeight) {
		this.slots = slots;
		this.x = x;
		this.y = y;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.width = gridWidth * 18 + SCROLL_BAR_WIDTH;
		this.height = gridHeight * 18;
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		// Unimplemented
		return Optional.empty();
	}

	@Override
	public ScreenPos getPosition() {
		return new ScreenPos(x, y);
	}

	@Override
	public IScrollGridWidget setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return this;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public ScreenRect getScreenRectangle() {
		return new ScreenRect(getPosition(), getWidth(), getHeight());
	}

	// [长期记忆: 005] JEI 15.62 把 IPlaceable#setPosition(6 参,区域+对齐)从 default
	// 改为 abstract,经 IScrollGridWidget extends IPlaceable 传导到本类,必须补真实现。
	// 语义同 JemiPlaceable:按自身 width/height 在区域内对齐后委托 setPosition(int, int)。
	// @Override 合法(15.20 里该 default 已存在);返回 IScrollGridWidget 覆盖 15.62 中
	// IScrollGridWidget 的协变重声明,编译器同时生成擦除为 IPlaceable 返回类型的桥方法,
	// 两条调用路径(经 IScrollGridWidget / 经 raw IPlaceable)的描述符都被满足。
	@Override
	public IScrollGridWidget setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
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
