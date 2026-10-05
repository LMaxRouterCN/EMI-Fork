package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.OffsetDrawable;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.TankInfo;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

public class JemiRecipeSlotBuilder implements IRecipeSlotBuilder {
	public final JemiIngredientAcceptor acceptor;
	public boolean large = false, defaultBackground = false;
	public int x, y;
	public Optional<String> name = Optional.empty();
	public IRecipeSlotTooltipCallback tooltipCallback;
	public IRecipeSlotRichTooltipCallback richTooltipCallback;
	public OffsetDrawable background, overlay;
	public Map<IIngredientType<?>, IngredientRenderer<?>> renderers; 
	public TankInfo tankInfo;

	public JemiRecipeSlotBuilder(RecipeIngredientRole role, int x, int y) {
		this.acceptor = new JemiIngredientAcceptor(role);
		this.x = x;
		this.y = y;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		acceptor.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		acceptor.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		acceptor.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		acceptor.addFluidStack(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		acceptor.addFluidStack(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, NbtCompound tag) {
		acceptor.addFluidStack(fluid, amount, tag);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		name = Optional.ofNullable(slotName);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		this.background = new OffsetDrawable(background, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		this.overlay = new OffsetDrawable(overlay, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		this.tankInfo = new TankInfo(width, height, capacity, showCapacity);
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType,
			IIngredientRenderer<T> ingredientRenderer) {
		if (renderers == null) {
			renderers = Maps.newHashMap();
		}
		renderers.put(ingredientType, new IngredientRenderer<T>(ingredientType, ingredientRenderer));
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		acceptor.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		acceptor.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		richTooltipCallback = tooltipCallback;
		return this;
	}

	@Override
	public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return this;
	}

	@Override
	public int getWidth() {
		return large ? 26 : 18;
	}

	@Override
	public int getHeight() {
		return large ? 26 : 18;
	}

	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		this.defaultBackground = true;
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		this.defaultBackground = true;
		this.large = true;
		return this;
	}

	// [长期记忆: 005] 残余风险(源码级不可实现,实测 T1:15.20 类路径无 TilingDirection):
	// JEI 15.62 新增重载 setFluidRenderer(long, boolean, int, int, TilingDirection) 的参数类型
	// 是 15.62 新增类型,在 15.20 编译面下无法写出该签名,故本类不实现;
	// 仅当 JEI 插件主动调用该 15.62 新 API 时才会 AbstractMethodError。
	// 同根残余风险:IRecipeExtrasBuilder 的 6 个 addXXXWidget(返回 IDrawableWidget)、
	// ITextWidget.setTooltip(IRecipeWidgetTooltipCallback) —— 详见 GOAL-PLAN.md。

	// JEI 15.62 把 IPlaceable#setPosition(6 参,区域+对齐)从 default 改为 abstract,
	// 经 IRecipeSlotBuilder extends IPlaceable 传导到本类,必须补真实现:
	// 按槽位自身尺寸(getWidth/getHeight)在区域内对齐,算出绝对坐标后委托 setPosition(int, int)。
	// @Override 合法(15.20 里该 default 已存在);返回 IRecipeSlotBuilder 覆盖 15.62 中
	// IRecipeSlotBuilder 的协变重声明,编译器同时生成擦除为 IPlaceable 返回类型的桥方法,
	// 经 IRecipeSlotBuilder 与经 raw IPlaceable 两条调用路径的描述符都被满足。
	@Override
	public IRecipeSlotBuilder setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		int x = areaX;
		if (horizontalAlignment == HorizontalAlignment.CENTER) {
			x += (areaWidth - getWidth()) / 2;
		} else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
			x += areaWidth - getWidth();
		}
		int y = areaY;
		if (verticalAlignment == VerticalAlignment.CENTER) {
			y += (areaHeight - getHeight()) / 2;
		} else if (verticalAlignment == VerticalAlignment.BOTTOM) {
			y += areaHeight - getHeight();
		}
		return setPosition(x, y);
	}
}
