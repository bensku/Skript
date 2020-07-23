package ch.njol.skript.expressions;

import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Roman Numeral")
@Description("The Roman Numeral of an Arabic Numeral")
@Examples("set {_r} to roman numeral of 49")
@Since("INSERT VERSION")
public class ExprRomanNumeral extends SimplePropertyExpression<Number, String> {
	
	private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>() {{
		put(1000, "M");
		put(900, "CM");
		put(500, "D");
		put(400, "CD");
		put(100, "C");
		put(90, "XC");
		put(50, "L");
		put(40, "XL");
		put(10, "X");
		put(9, "IX");
		put(5, "V");
		put(4, "IV");
		put(1, "I");
	}};
	
	static {
		register(ExprRomanNumeral.class, String.class, "roman num(ber|eral)", "number");
	}
	
	public static String toRoman(int number) {
		int l = map.floorKey(number);
		if (number == l) {
			return map.get(number);
		}
		return map.get(l) + toRoman(number - l);
	}
	
	@Nullable
	@Override
	public String convert(Number number) {
		return toRoman(number.intValue());
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "roman numeral";
	}
	
}
