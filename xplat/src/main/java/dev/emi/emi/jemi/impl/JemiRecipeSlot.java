package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.tag.TagKey;

@SuppressWarnings("unchecked")
public class JemiRecipeSlot implements IRecipeSlotView {
	public final RecipeIngredientRole role;
	public final boolean large, defaultBackground;
	public final int x, y;
	public final Optional<String> name;
	public final IRecipeSlotTooltipCallback tooltipCallback;
	public final IRecipeSlotRichTooltipCallback richTooltipCallback;
	public final OffsetDrawable background, overlay;
	public final Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public final TankInfo tankInfo;
	public final EmiIngredient stack;
	public SlotWidget widget;
	public int highlight = 0;

	public JemiRecipeSlot(JemiRecipeSlotBuilder builder) {
		this.role = builder.acceptor.role;
		this.large = builder.large;
		this.defaultBackground = builder.defaultBackground;
		this.x = builder.x;
		this.y = builder.y;
		this.name = builder.name;
		this.tooltipCallback = builder.tooltipCallback;
		this.richTooltipCallback = builder.richTooltipCallback;
		this.background = builder.background;
		this.overlay = builder.overlay;
		this.renderers = builder.renderers;
		this.tankInfo = builder.tankInfo;
		this.stack = builder.acceptor.build();
	}

	public JemiRecipeSlot(RecipeIngredientRole role, EmiStack stack) {
		this.role = role;
		this.large = false;
		this.defaultBackground = false;
		this.x = 0;
		this.y = 0;
		this.name = Optional.empty();
		this.tooltipCallback = null;
		this.richTooltipCallback = null;
		this.background = null;
		this.overlay = null;
		this.renderers = null;
		this.tankInfo = null;
		this.stack = stack;
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return (Stream<T>) getAllIngredients().filter(t -> t.getType() == ingredientType).map(t -> t.getIngredient());
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return stack.getEmiStacks().stream().map(JemiUtil::getTyped).filter(Optional::isPresent).map(Optional::get);
	}

	// [长期记忆: 005] JEI 15.34.0 新增接口方法:IRecipeSlotView#getAllIngredientsList。
	// EMI 目前仍编译在 JEI 15.20(loom 1.7 无法解析 15.62 工件),旧接口中不存在此方法,
	// 因此这里【不能加 @Override】;JVM 方法解析按"方法名+参数擦除描述符"命中,
	// 在 JEI 15.62 运行环境下会正常作为接口实现被调用。将来升级 loom/jei_version 后应补回 @Override。
	// 语义(对齐 JEI 15.62 源码 javadoc):返回槽位全部候选成分(轮换全集),空位以 null 占位,
	// 返回不可变列表。与上面 getAllIngredients() 的区别:这里不丢弃空位(EmiStack.EMPTY -> null),
	// 保证 JEI 按索引对应槽位布局(例如 3x3 合成补位的空槽)不发生错位。
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		// 显式类型 witness:方法引用的通配符返回类型会被捕获成 CAP#1,而 List 不变,
		// List<ITypedIngredient<CAP#1>> 无法直接赋给 List<@Nullable ITypedIngredient<?>>;
		// 把流元素类型显式钉为 @Nullable ITypedIngredient<?>,toList 才能推出正确的目标类型。
		return stack.getEmiStacks().stream()
			.<@Nullable ITypedIngredient<?>>map(es -> JemiUtil.getTyped(es).orElse(null))
			.toList();
	}

	// JEI 15.52.0 新增接口方法:返回"当前显示成分所在组"的完整候选集
	// (JEI 为渲染性能会限制槽位轮换范围,此方法要求绕过限制给出完整一组)。
	// EMI 槽位没有轮换限制的概念,完整候选集就是 getAllIngredients(),直接委托。
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return getAllIngredients();
	}

	// JEI 15.53.0 新增接口方法:若槽位所有候选成分同属一个物品标签,返回该标签。
	// EmiIngredient 没有对应的"共同标签"查询语义(只有 EmiIngredient.of(TagKey) 工厂方法),
	// 给不出有意义的值;按 JEI javadoc,empty 表示"该槽位没有单一标签",是文档定义的合法空值。
	// 擦除后签名 ()Ljava/util/Optional; 与接口声明一致,泛型参数不影响 JVM 方法解析。
	public Optional<TagKey<?>> getTagKey() {
		return Optional.empty();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		Optional<ITypedIngredient<?>> ing = getDisplayedIngredient();
		if (ing.isPresent() && ing.get().getType() == ingredientType) {
			return (Optional<T>) Optional.of(ing.get().getIngredient());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return JemiUtil.getTyped(stack.getEmiStacks().get(0));
	}

	@Override
	public Optional<String> getSlotName() {
		return name;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(DrawContext raw, int color) {
		this.highlight = color;
	}

	public static record OffsetDrawable(IDrawable drawable, int xOff, int yOff){
	}

	public static record IngredientRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer){
	}

	public static record TankInfo(int width, int height, long capacity, boolean showCapacity) {
	}
}
