package dev.emi.emi.jemi.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.runtime.IJeiKeyMapping;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class JemiTooltipBuilder implements ITooltipBuilder {
	public final List<TooltipComponent> tooltip = Lists.newArrayList();
	private final List<Text> legacyText = Lists.newArrayList();

	@Override
	public void add(StringVisitable component) {
		// JEI allows non-text StringVisitable... Minecraft's methods don't easily
		if (component instanceof Text text) {
			tooltip.add(TooltipComponent.of(text.asOrderedText()));
			legacyText.add(text);
		}
	}

	@Override
	public void addAll(Collection<? extends StringVisitable> components) {
		for (StringVisitable v : components) {
			add(v);
		}
	}

	@Override
	public void add(TooltipData data) {
		try {
			tooltip.add(TooltipComponent.of(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		// EMI's methods bypass the vanilla tooltip render which accepts a stack, so this will do nothing
	}

	@Override
	public List<Text> toLegacyToComponents() {
		return legacyText;
	}

	@Override
	public void removeAll(List<Text> components) {
		// EMI does not support tooltip removeal
	}

	// [长期记忆: 005] JEI 15.62 新增接口方法(15.20 无),EMI 编译在 15.20 故不加 @Override;
	// 运行时按"方法名+完整擦除描述符(含返回类型)"命中,升 loom/jei 后应补 @Override。
	// JEI 用它追加按键提示行;EMI 的 tooltip 走自己的渲染管线,此处忽略。
	public void addKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping) {
	}

	// JEI 15.62 新增:清空全部已累积的 tooltip 行。EMI 有两条内部存储(组件/遗留文本),一并清空。
	public void clear() {
		tooltip.clear();
		legacyText.clear();
	}

	// JEI 15.62 新增:仅清除 setIngredient 设置的成分;EMI 的 setIngredient 本就是 no-op,对称 no-op。
	public void clearIngredient() {
	}

	// JEI 15.62 新增:返回当前累积的行(左=文本,右=数据)。EMI 的存储把文本行与数据行分仓且不记
	// 交错顺序,这里返回文本行的 Either.left 映射;TooltipData 行无法还原为右值,省略
	// (只影响 JEI 侧对该查询的数据完整性,不构成崩溃面)。
	public List<Either<StringVisitable, TooltipData>> getLines() {
		return legacyText.stream().map(t -> Either.<StringVisitable, TooltipData>left(t)).toList();
	}
}
