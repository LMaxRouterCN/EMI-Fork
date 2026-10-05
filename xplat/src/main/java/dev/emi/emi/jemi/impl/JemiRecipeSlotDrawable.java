package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;

public class JemiRecipeSlotDrawable implements IRecipeSlotDrawable {
	public JemiSlotWidget widget;
	public List<IIngredientConsumer> overrides = Lists.newArrayList();

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return widget.slot.getAllIngredients();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return widget.slot.getDisplayedIngredient();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return widget.slot.getRole();
	}

	@Override
	public void drawHighlight(DrawContext raw, int color) {
		widget.slot.drawHighlight(raw, color);
	}

	@Override
	public Optional<String> getSlotName() {
		return widget.slot.getSlotName();
	}

	@Override
	public void draw(DrawContext raw) {
		// I don't think I will
	}

	@Override
	public void drawHoverOverlays(DrawContext raw) {
		// I don't think I will
	}

	@Override
	public List<Text> getTooltip() {
		// Unimplemented
		// Mutable because who knows
		return Lists.newArrayList();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		// Unimplemented
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return widget.getBounds().contains((int) mouseX, (int) mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
		// Nope
	}

	@Override
	public IIngredientConsumer createDisplayOverrides() {
		// "Implemented" but also just ignored
		JemiIngredientConsumer consumer = new JemiIngredientConsumer();
		overrides.add(consumer);
		return consumer;
	}

	@Override
	public void clearDisplayOverrides() {
		overrides.clear();
	}

	@Override
	public Rect2i getRect() {
		Bounds bounds = widget.getBounds();
		return new Rect2i(bounds.x(), bounds.y(), bounds.width(), bounds.height());
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		return getRect();
	}

	// [长期记忆: 005] JEI 15.34.0 起接口 IRecipeSlotView 新增 abstract 方法,
	// 经 IRecipeSlotDrawable extends IRecipeSlotView 传导到本实现类(15.20 时只有 8 个方法)。
	// EMI 编译在 JEI 15.20(loom 1.7 无法解析 15.62 工件),故不加 @Override;
	// JVM 按"方法名+擦除描述符"命中,15.62 运行时正常作为接口实现被调用。升 loom 后补 @Override。
	// 三个方法全部委托给槽位本体 JemiRecipeSlot(其中已有同款实现,语义见该类注释)。
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return widget.slot.getAllIngredientsList();
	}

	// JEI 15.52.0 新增:完整候选集;EMI 槽位无轮换限制,委托槽位本体即可。
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return widget.slot.getDisplayedIngredients();
	}

	// JEI 15.53.0 新增:槽位共同标签;EMI 无此概念,委托槽位本体(恒 empty)。
	public Optional<TagKey<?>> getTagKey() {
		return widget.slot.getTagKey();
	}

	// JEI 15.20 之后新增:JEI 用它在指定坐标绘制槽位 tooltip。
	// EMI 的 tooltip 走自己的渲染路径(与现有 getTooltip() 空实现同理),此处 no-op。
	public void drawTooltip(DrawContext guiGraphics, int mouseX, int mouseY) {
	}
}
