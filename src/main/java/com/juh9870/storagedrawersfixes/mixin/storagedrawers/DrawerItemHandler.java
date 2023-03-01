package com.juh9870.storagedrawersfixes.mixin.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

// Basically remixing (almost) all public methods
// This works by completely removing virtual slot
@Mixin(com.jaquadro.minecraft.storagedrawers.capabilities.DrawerItemHandler.class)
public class DrawerItemHandler {

    @Shadow(remap = false)
    private IDrawerGroup group;

    @Inject(at = @At("HEAD"), method = "getSlots()I", remap = false, cancellable = true)
    public void drawersfix_getSlots(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(!this.group.isGroupValid() ? 0 : this.group.getDrawerCount());
    }

    @Inject(at = @At("HEAD"), method = "getStackInSlot(I)Lnet/minecraft/item/ItemStack;", remap = false, cancellable = true)
    public void drawersfix_insertItem(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!this.group.isGroupValid()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        int[] order = this.group.getAccessibleDrawerSlots();
        slot = slot >= 0 && slot < order.length ? order[slot] : -1;
        IDrawer drawer = this.group.getDrawer(slot);
        if (drawer.isEnabled() && !drawer.isEmpty()) {
            ItemStack stack = drawer.getStoredItemPrototype().copy();
            stack.setCount(drawer.getStoredItemCount());
            cir.setReturnValue(stack);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(at = @At("HEAD"), method = "insertItem(ILnet/minecraft/item/ItemStack;Z)Lnet/minecraft/item/ItemStack;", remap = false, cancellable = true)
    public void drawersfix_getSlots(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!this.group.isGroupValid()) {
            cir.setReturnValue(stack);
            return;
        }

        IDrawer drawer = this.getDrawer(slot);

        if (!drawer.canItemBeStored(stack)) {
            cir.setReturnValue(stack);
            return;
        }

        if (drawer.isEmpty() && !simulate) {
            drawer = drawer.setStoredItem(stack);
        }

        boolean empty = drawer.isEmpty();
        int remainder = simulate ?
                Math.max(stack.getCount() - (empty ?
                        drawer.getAcceptingMaxCapacity(stack) :
                        drawer.getAcceptingRemainingCapacity()
                ), 0) :
                drawer.adjustStoredItemCount(stack.getCount());
        if (remainder == stack.getCount()) {
            cir.setReturnValue(stack);
        } else {
            cir.setReturnValue(remainder == 0 ? ItemStack.EMPTY : stackResult(stack, remainder));
        }
    }

    @Inject(at = @At("HEAD"), method = "extractItem(IIZ)Lnet/minecraft/item/ItemStack;", remap = false, cancellable = true)
    public void drawersfix_extractItem(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!this.group.isGroupValid()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        IDrawer drawer = this.getDrawer(slot);
        if (drawer.isEnabled() && !drawer.isEmpty() && drawer.getStoredItemCount() != 0) {
            ItemStack prototype = drawer.getStoredItemPrototype();
            int remaining = simulate ? Math.max(amount - drawer.getStoredItemCount(), 0) : drawer.adjustStoredItemCount(-amount);
            cir.setReturnValue(stackResult(prototype, amount - remaining));
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(at = @At("HEAD"), method = "getSlotLimit(I)I", remap = false, cancellable = true)
    public void drawersfix_getSlotLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        if (!this.group.isGroupValid()) {
            cir.setReturnValue(0);
            return;
        }
        IDrawer drawer = this.getDrawer(slot);
        if (!drawer.isEnabled()) {
            cir.setReturnValue(0);
        } else {
            cir.setReturnValue(drawer.isEmpty() ? drawer.getMaxCapacity(ItemStack.EMPTY) : drawer.getMaxCapacity());
        }
    }

    private IDrawer getDrawer(int slot) {
        int[] order = this.group.getAccessibleDrawerSlots();
        slot = slot >= 0 && slot < order.length ? order[slot] : -1;
        return this.group.getDrawer(slot);
    }

    private static ItemStack stackResult(@Nonnull ItemStack stack, int amount) {
        ItemStack result = stack.copy();
        result.setCount(amount);
        return result;
    }
}