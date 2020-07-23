package ch.njol.skript.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Arabic Numeral")
@Description("Returns an arabic numeral from a roman numeral.")
@Examples({"set {_n} to arabic numeral of \"VI\""})
@Since("INSERT VERSION")
public class ExprRomanNumeralFrom extends SimplePropertyExpression<String, Number> {
	
	static {
		register(ExprRomanNumeralFrom.class, Number.class, "arabic num(ber|eral)", "string");
	}
	
	private int fromRoman(String i) {
		if (i.startsWith("M")) return fromRoman(i.substring(1)) + 1000;
		if (i.startsWith("CM")) return fromRoman(i.substring(2)) + 900;
		if (i.startsWith("D")) return fromRoman(i.substring(1)) + 500;
		if (i.startsWith("CD")) return fromRoman(i.substring(2)) + 400;
		if (i.startsWith("C")) return fromRoman(i.substring(1)) + 100;
		if (i.startsWith("XC")) return fromRoman(i.substring(2)) + 90;
		if (i.startsWith("L")) return fromRoman(i.substring(1)) + 50;
		if (i.startsWith("XL")) return fromRoman(i.substring(2)) + 40;
		if (i.startsWith("X")) return fromRoman(i.substring(1)) + 10;
		if (i.startsWith("IX")) return fromRoman(i.substring(2)) + 9;
		if (i.startsWith("V")) return fromRoman(i.substring(1)) + 5;
		if (i.startsWith("IV")) return fromRoman(i.substring(2)) + 4;
		if (i.startsWith("I")) return fromRoman(i.substring(1)) + 1;
		return 0;
	}
	
	@Nullable
	@Override
	public Number convert(String s) {
		return fromRoman(s);
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "arabic numeral";
	}
	
}
