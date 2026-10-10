package com.liquidpackage.config;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.liquidpackage.LiquidPackage;

public class Config implements Iterable<Config.Item> {
    public static record Item(String liquidId, int liquidPerPackage, int fuelTime) {
        public boolean isFuel() {
            return fuelTime > 0;
        }
    }

    public static final int CAPACITY = 20;
    public static final Config DEFAULT;
    static {
        DEFAULT = new Config();
        DEFAULT.setEmptyShellFuelTime(200);
        DEFAULT.push(new Item("minecraft:water", 15, 0));
        DEFAULT.push(new Item("minecraft:lava", 1, 20000));
    }

    private int size = 0;

    private int emptyShellFuelTime = 0;

    private final Item[] items = new Item[CAPACITY];

    public Config() {
    }

    public void push(Item item) {
        if (item == null)
            return;

        if (size >= CAPACITY) {
            LiquidPackage.LOGGER.error("Bad config. Maximum records is {}", CAPACITY);
            return;
        }

        this.items[size] = item;
        size++;
    }

    public int emptyShellFuelTime() {
        return this.emptyShellFuelTime;
    }

    public void setEmptyShellFuelTime(int emptyShellFuelTime) {
        this.emptyShellFuelTime = emptyShellFuelTime;
    }

    public int size() {
        return this.size;
    }

    public Item get(int index) {
        return items[index];
    }

    @Override
    public Iterator<Item> iterator() {
        return new Iterator<Config.Item>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Item next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return items[currentIndex++];
            }

        };
        // return Arrays.asList(items).iterator();
    }
}
