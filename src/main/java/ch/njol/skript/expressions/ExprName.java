/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Name / Display Name")
@Description({"Represents the Minecraft account, display or tab list name of a player, or the custom name of an item, entity or inventory.",
		"",
		"<ul>",
		"\t<li><strong>Players</strong>",
		"\t\t<ul>",
		"\t\t\t<li><strong>Name:</strong> The Minecraft account name of the player. Can't be changed, but 'display name' can be changed.</li>",
		"\t\t\t<li><strong>Display Name:</strong> The name of the player that is displayed in messages. " +
			"This name can be changed freely and can include colour codes, and is shared among all plugins (e.g. chat plugins will use the display name).</li>",
		"\t\t</ul>",
		"\t</li>",
		"\t<li><strong>Entities</strong>",
		"\t\t<ul>",
		"\t\t\t<li><strong>Name:</strong> The custom name of the entity. Can be changed. But for living entities, " +
			"the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.</li>",
		"\t\t\t<li><strong>Display Name:</strong> The custom name of the entity. Can be changed, " +
			"which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.</li>",
		"\t\t</ul>",
		"\t</li>",
		"\t<li><strong>Items</strong>",
		"\t\t<ul>",
		"\t\t\t<li><strong>Name and Display Name:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.</li>",
		"\t\t</ul>",
		"\t</li>",
		"\t<li><strong>Inventories</strong>",
		"\t\t<ul>",
		"\t\t\t<li><strong>Name and Display Name:</strong> The name/title of the inventory. " +
			"Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.</li>",
		"\t\t</ul>",
		"\t</li>",
		"</ul>"})
@Examples({"on join:",
		"	player has permission \"name.red\"",
		"	set the player's display name to \"<red>[admin] <gold>%name of player%\"",
		"	set the player's tab list name to \"<green>%player's name%\"",
		"set the name of the player's tool to \"Legendary Sword of Awesomeness\""})
@Since("before 2.1, 2.2-dev20 (inventory name), INSERT VERSION (non-living entity support, changeable inventory name)")
public class ExprName extends SimplePropertyExpression<Object, String> {

	@Nullable
	static final MethodHandle TITLE_METHOD;

	static {
		MethodHandle _METHOD = null;
		try {
			_METHOD = MethodHandles.lookup().findVirtual(Inventory.class, "getTitle", MethodType.methodType(String.class));
		} catch (IllegalAccessException | NoSuchMethodException ignored) {}
		TITLE_METHOD = _METHOD;
	}

	private final static int ITEM = 1, ENTITY = 2, PLAYER = 4, INVENTORY = 8;
	final static String[] types = {"itemstacks/slots", "entities", "players", "inventories"};

	private enum NameType {
		NAME("name", "name[s]", PLAYER | ITEM | ENTITY | INVENTORY, ITEM | ENTITY | INVENTORY) {
			@Override
			void set(@Nullable Object o, @Nullable String name) {
				if (o == null)
					return;
				if (o instanceof Entity) {
					((Entity) o).setCustomName(name);
					if (o instanceof LivingEntity)
						((LivingEntity) o).setRemoveWhenFarAway(name == null);
				} else if (o instanceof ItemType) {
					ItemMeta m = ((ItemType) o).getItemMeta();
					m.setDisplayName(name);
					((ItemType) o).setItemMeta(m);
				} else if (o instanceof ItemStack) {
					ItemMeta m = ((ItemStack) o).getItemMeta();
					if (m != null) {
						m.setDisplayName(name);
						((ItemStack) o).setItemMeta(m);
					}
				} else if (o instanceof Inventory) {
					Inventory inventory = ((Inventory) o);
					List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
					if (viewers.isEmpty())
						return;
					Inventory copy;
					InventoryType invType = inventory.getType();
					if (!invType.isCreatable())
						return;
					if (invType == InventoryType.CHEST) {
						if (name == null)
							copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize());
						else
							copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
					} else {
						if (name == null)
							copy = Bukkit.createInventory(inventory.getHolder(), invType);
						else
							copy = Bukkit.createInventory(inventory.getHolder(), invType, name);
					}
					copy.setContents(inventory.getContents());
					viewers.forEach(human -> human.openInventory(copy));
				} else {
					assert false;
				}
			}

			@Override
			@Nullable
			String get(@Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getName();
				} else if (o instanceof Entity) {
					return ((Entity) o).getCustomName();
				} else if (o instanceof ItemType) {
					ItemMeta m = ((ItemType) o).getItemMeta();
					return !m.hasDisplayName() ? null : m.getDisplayName();
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					ItemMeta m = ((ItemStack) o).getItemMeta();
					return m == null || !m.hasDisplayName() ? null : m.getDisplayName();
				} else if (o instanceof Inventory) {
					if (TITLE_METHOD != null) {
						try {
							return ((String) TITLE_METHOD.invoke(o));
						} catch (IllegalAccessException e) {
							assert false;
							return null;
						} catch (Throwable e) {
							Skript.exception(e);
							return null;
						}
					}
					return null;
				} else {
					assert false;
					return null;
				}
			}
		},
		DISPLAY_NAME("display name", "(display|nick|chat)[ ]name[s]", PLAYER | ITEM | ENTITY | INVENTORY, PLAYER | ITEM | ENTITY | INVENTORY) {
			@Override
			void set(@Nullable Object o, @Nullable String name) {
				if (o == null)
					return;
				if (o instanceof Player) {
					((Player) o).setDisplayName(name == null ? ((Player) o).getName() : name + ChatColor.RESET);
				} else if (o instanceof Entity) {
					((Entity) o).setCustomName(name);
					((Entity) o).setCustomNameVisible(name != null);
					if (o instanceof LivingEntity)
						((LivingEntity) o).setRemoveWhenFarAway(name == null);
				} else if (o instanceof ItemStack) {
					ItemMeta m = ((ItemStack) o).getItemMeta();
					if (m != null) {
						m.setDisplayName(name);
						((ItemStack) o).setItemMeta(m);
					}
				} else if (o instanceof Inventory) {
					Inventory inventory = ((Inventory) o);
					List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
					if (viewers.isEmpty())
						return;
					Inventory copy;
					InventoryType invType = inventory.getType();
					if (!invType.isCreatable())
						return;
					if (invType == InventoryType.CHEST) {
						if (name == null)
							copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize());
						else
							copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
					} else {
						if (name == null)
							copy = Bukkit.createInventory(inventory.getHolder(), invType);
						else
							copy = Bukkit.createInventory(inventory.getHolder(), invType, name);
					}
					copy.setContents(inventory.getContents());
					viewers.forEach(human -> human.openInventory(copy));
				} else {
					assert false;
				}
			}

			@Override
			@Nullable
			String get(@Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getDisplayName();
				} else if (o instanceof Entity) {
					return ((Entity) o).getCustomName();
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					ItemMeta m = ((ItemStack) o).getItemMeta();
					return m == null || !m.hasDisplayName() ? null : m.getDisplayName();
				} else if (o instanceof Inventory) {
					if (TITLE_METHOD != null) {
						try {
							return ((String) TITLE_METHOD.invoke(o));
						} catch (IllegalAccessException e) {
							assert false;
							return null;
						} catch (Throwable e) {
							Skript.exception(e);
							return null;
						}
					}
					return null;
				} else {
					assert false;
					return null;
				}
			}
		},
		TABLIST_NAME("player list name", "(player|tab)[ ]list name[s]", PLAYER, PLAYER) {
			@Override
			void set(@Nullable Object o, @Nullable String name) {
				if (o == null)
					return;
				if (o instanceof Player) {
					try {
						((Player) o).setPlayerListName(name == null ? "" : name);
					} catch (IllegalArgumentException ignored) {}
				} else {
					assert false;
				}
			}

			@Override
			@Nullable
			String get(@Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getPlayerListName();
				} else {
					assert false;
					return null;
				}
			}
		};

		final String name;
		final String pattern;
		final int from;
		final int acceptChange;

		NameType(String name, String pattern, int from, int change) {
			this.name = name;
			this.pattern = "(" + ordinal() + "¦)" + pattern;
			this.from = from;
			acceptChange = change;
		}

		abstract void set(@Nullable Object o, @Nullable String s);

		@Nullable
		abstract String get(@Nullable Object o);

		String getFrom() {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < types.length; i++) {
				if ((from & (1 << i)) == 0)
					continue;
				if ((1 << i) == ITEM && !Skript.isRunningMinecraft(1, 4, 5))
					continue;
				if ((1 << i) == ENTITY && !Skript.isRunningMinecraft(1, 5))
					continue;
				if (b.length() != 0)
					b.append("/");
				b.append(types[i]);
			}
			return "" + b;
		}
	}

	static {
		for (NameType n : NameType.values())
			register(ExprName.class, String.class, n.pattern, n.getFrom());
	}

	@SuppressWarnings("null")
	private NameType type;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = NameType.values()[parseResult.mark];
		if (exprs[0] instanceof Variable)
			setExpr(exprs[0].getConvertedExpression(Object.class));
		else
			setExpr(exprs[0]);
		return true;
	}

	@Override
	@Nullable
	public String convert(Object o) {
		return type.get(o instanceof Slot ? ((Slot) o).getItem() : o);
	}

	private int changeType = 0;

	// TODO find a better method for handling changes (in general)
	// e.g. a Changer that takes an object and returns another which should then be saved if applicable (the Changer includes the ChangeMode)
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.DELETE && (type.acceptChange & ~PLAYER) != 0 || mode == ChangeMode.RESET)
			return new Class[0];
		if (mode != ChangeMode.SET)
			return null;
		if ((type.acceptChange & PLAYER) != 0 && Player.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = PLAYER;
		} else if ((type.acceptChange & INVENTORY) != 0 && Inventory.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = INVENTORY;
		} else if ((type.acceptChange & ITEM) != 0 && (getExpr().isSingle() && ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class, ItemType.class) || Slot.class.isAssignableFrom(getExpr().getReturnType()))) {
			changeType = ITEM;
		} else if ((type.acceptChange & ENTITY) != 0 && Entity.class.isAssignableFrom(getExpr().getReturnType())) {
			if (type == NameType.NAME && Player.class.isAssignableFrom(getExpr().getReturnType())) {
				Skript.error("Can't change the Minecraft name of a player. Change the 'display name' or 'tab list name' instead.");
				return null;
			}
			changeType = ENTITY;
		}
		return changeType == 0 ? null : CollectionUtils.array(String.class);
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		String name = delta == null ? null : (String) delta[0];
		if (changeType == ITEM) {
			if (Slot.class.isAssignableFrom(getExpr().getReturnType())) {
				for (Slot s : (Slot[]) getExpr().getArray(e)) {
					ItemStack i = s.getItem();
					type.set(i, name);
					s.setItem(i);
				}
			} else {
				Object i = getExpr().getSingle(e);
				if (i instanceof ItemType) {
					type.set(i, name);
					getExpr().change(e, new ItemType[] {(ItemType) i}, ChangeMode.SET);
					return;
				}

				if (!(i instanceof ItemStack) && !(i instanceof Slot))
					return;
				ItemStack is = i instanceof Slot ? ((Slot) i).getItem() : (ItemStack) i;
				type.set(is, name);
				if (i instanceof Slot)
					((Slot) i).setItem(is);
				else if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class))
					getExpr().change(e, new Object[] {i}, ChangeMode.SET);
				else
					getExpr().change(e, new ItemType[] {new ItemType((ItemStack) i)}, ChangeMode.SET);
			}
		} else {
			for (Object o : getExpr().getArray(e)) {
				if (o instanceof Entity || o instanceof Inventory)
					type.set(o, name);
			}
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return type.name;
	}

}
