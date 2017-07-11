package com.brandon3055.brandonscore.blocks;

import cofh.redstoneflux.impl.EnergyStorage;
import com.brandon3055.brandonscore.lib.EnergyHandlerWrapper;
import com.brandon3055.brandonscore.lib.EnergyHelper;
import com.brandon3055.brandonscore.lib.datamanager.ManagedInt;
import com.brandon3055.brandonscore.lib.datamanager.TileDataOptions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

/**
 * Created by brandon3055 on 28/3/2016.
 * The base tile for energy providers and receivers that do not have inventories. When extending ether implement implement IEnergyReceiver,
 * IEnergyProvider or both.
 */
public class TileEnergyBase extends TileBCBase {

    public ManagedInt energySync = null;
    protected EnergyStorage energyStorage = new EnergyStorage(0, 0, 0);

    @Override
    public void update() {
        super.update();
        if (energySync != null) {
            if (world.isRemote) {
                energyStorage.setEnergyStored(energySync.value);
            } else {
                energySync.value = energyStorage.getEnergyStored();
            }
        }
    }

    public TileDataOptions<ManagedInt> setEnergySyncMode() {
        TileDataOptions<ManagedInt> options = dataManager.register("anInt", new ManagedInt(0));
        energySync = options.finish();
        return options;
    }

    protected void setCapacityAndTransfer(int capacity, int receive, int extract) {
        energyStorage.setCapacity(capacity);
        energyStorage.setMaxReceive(receive);
        energyStorage.setMaxExtract(extract);
    }

    public int getEnergyStored(EnumFacing from) {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored(EnumFacing from) {
        return energyStorage.getMaxEnergyStored();
    }

    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return energyStorage.extractEnergy(maxExtract, simulate);
    }

    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public NBTTagCompound writeToItemStack(ItemStack stack, boolean willHarvest) {
        NBTTagCompound dataTag = super.writeToItemStack(stack, willHarvest);
        energyStorage.writeToNBT(dataTag);
        return dataTag;
    }

    @Override
    public NBTTagCompound readFromItemStack(ItemStack stack) {
        NBTTagCompound dataTag = super.readFromItemStack(stack);
        energyStorage.readFromNBT(dataTag);
        return dataTag;
    }

    @Override
    public void writeExtraNBT(NBTTagCompound compound) {
        super.writeExtraNBT(compound);
        energyStorage.writeToNBT(compound);
    }

    @Override
    public void readExtraNBT(NBTTagCompound compound) {
        super.readExtraNBT(compound);
        energyStorage.readFromNBT(compound);
    }

    protected int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    protected int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int sendEnergyToAll() {
        if (getEnergyStored() == 0) {
            return 0;
        }
        int i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(direction);
        }
        return i;
    }

    public int sendEnergyTo(EnumFacing side) {
        if (getEnergyStored() == 0) {
            return 0;
        }
        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null && EnergyHelper.canReceiveEnergy(tile)) {
            return EnergyHelper.insertEnergy(tile, getEnergyStored(), side.getOpposite(), false);
        }
        return 0;
    }

    public static int sendEnergyTo(IBlockAccess world, BlockPos pos, int maxSend, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null && EnergyHelper.canReceiveEnergy(tile, side.getOpposite())) {
            return EnergyHelper.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static int sendEnergyToAll(IBlockAccess world, BlockPos pos, int maxSend) {
        int i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(world, pos, maxSend - i, direction);
        }
        return i;
    }

    public int extractEnergyFromItem(ItemStack stack, int maxExtract, boolean simulate) {
        if (EnergyHelper.isEnergyStack(stack)) {
            return EnergyHelper.extractEnergy(stack, maxExtract, simulate);
        }
        return 0;
    }

    //region Capability

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(new EnergyHandlerWrapper(this, facing));
        }

        return super.getCapability(capability, facing);
    }

    //endregion
}
