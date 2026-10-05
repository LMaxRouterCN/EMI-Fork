package dev.emi.emi.jemi.impl.extras;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.ITextWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.StringVisitable;

public class JemiTextWidget extends JemiPlaceable<ITextWidget> implements ITextWidget {
	public int color = 0xffffffff;
	public boolean shadow = true;
	public int spacing = 0;
	public HorizontalAlignment horizontal = HorizontalAlignment.LEFT;
	public VerticalAlignment vertical = VerticalAlignment.TOP;
	public List<StringVisitable> text;

	public JemiTextWidget(List<StringVisitable> text, int width, int height) {
		super(width, height);
		this.text = text;
	}

	@Override
	public ITextWidget setFont(TextRenderer font) {
		// Unimplemented
		return this;
	}

	@Override
	public ITextWidget setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public ITextWidget setLineSpacing(int spacing) {
		this.spacing = spacing;
		return this;
	}

	@Override
	public ITextWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	@Override
	public ITextWidget setTextAlignment(HorizontalAlignment horizontalAlignment) {
		this.horizontal = horizontalAlignment;
		return this;
	}

	@Override
	public ITextWidget setTextAlignment(VerticalAlignment verticalAlignment) {
		this.vertical = verticalAlignment;
		return this;
	}

	// [长期记忆: 005] 以下为 JEI 15.62 传导到本类的新增/协变重声明方法(15.20 编译面
	// 无对应签名或签名擦除不同)。协变重声明让编译器自动生成桥方法,同时满足 15.62 中
	// 经 ITextWidget(经 IRecipeWidgetBuilder)与经 raw IPlaceable 两条调用路径的描述符。

	// ITextWidget 15.62 以协变返回类型 ITextWidget 重声明 setPosition(int,int)
	// (15.20 中经 IPlaceable<ITextWidget> 可见、擦除返回 IPlaceable,故 @Override 合法)。
	@Override
	public ITextWidget setPosition(int xPos, int yPos) {
		super.setPosition(xPos, yPos);
		return this;
	}

	// ITextWidget 15.62 以协变返回类型重声明 setPosition(6 参:区域+对齐)
	// (15.20 中是 IPlaceable 的 default,@Override 合法);委托 JemiPlaceable 的对齐实现。
	@Override
	public ITextWidget setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		super.setPosition(areaX, areaY, areaWidth, areaHeight, horizontalAlignment, verticalAlignment);
		return this;
	}

	// JEI 15.56.0 起新增(IRecipeWidgetBuilder 引入,15.20 无):设置控件 tooltip。
	// EMI 的文本控件渲染不经过 JEI tooltip 管线(与本类 setFont 的 stub 同理),no-op 保留链式调用。
	// 注意参数类型:class_5348 = StringVisitable(对照 JemiTooltipBuilder#add(StringVisitable) 坐实),非 Text。
	public ITextWidget setTooltip(StringVisitable tooltip) {
		return this;
	}

	public ITextWidget setTooltip(Collection<? extends StringVisitable> tooltip) {
		return this;
	}

	public ITextWidget setTooltip(TooltipComponent tooltip) {
		return this;
	}
	// 注:setTooltip(IRecipeWidgetTooltipCallback) 重载无法在本源码树下实现——
	// 参数类型是 15.56 新类型(实测 T2:15.20 类路径无此文件),签名写不出来(残余风险,见 GOAL-PLAN)。
	
}
