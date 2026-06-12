package gay.menkissing.pastelstorage.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Locale;
import java.util.Objects;

public final class FluidResource {
    private final Fluid fluid;
    private final DataComponentPatch components;
    private final int hashCode;

    private FluidResource(Fluid fluid, DataComponentPatch components) {
        this.hashCode = Objects.hash(fluid, components);
        this.fluid = fluid;
        this.components = components;
    }

    public boolean isBlank() {
        return fluid.isSame(Fluids.EMPTY);
    }

    public FluidStack makeStack(int size) {
        return size == 0 ? FluidStack.EMPTY : new FluidStack(fluid.builtInRegistryHolder(), size, components);
    }

    public boolean componentsMatch(DataComponentPatch other) {
        return this.components.equals(other);
    }

    public boolean sameAsStack(FluidStack stack) {
        return this.fluid.equals(stack.getFluid()) && this.componentsMatch(stack.getComponentsPatch());
    }

    public Fluid getFluid() {
        return fluid;
    }

    public DataComponentPatch getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        } else {
            FluidResource that = (FluidResource)obj;
            return this.hashCode == that.hashCode && this.fluid.equals(that.fluid) && componentsMatch(that.components);
        }
    }


    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public static final Codec<FluidResource> CODEC =
            RecordCodecBuilder.create(builder ->
                builder.group(
                    BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidResource::getFluid),
                    DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidResource::getComponents)
                ).apply(builder, FluidResource::new)
            );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.FLUID), FluidResource::getFluid,
                    DataComponentPatch.STREAM_CODEC, FluidResource::getComponents,
                    FluidResource::of
            );
            
    public static final FluidResource EMPTY = new FluidResource(Fluids.EMPTY, DataComponentPatch.EMPTY);
    
    public static FluidResource ofStack(FluidStack stack) {
        Objects.requireNonNull(stack, "Fluid stack may not be null");

        return new FluidResource(stack.getFluid(), stack.getComponentsPatch());
    }

    public static FluidResource of(Fluid fluid) {
        return of(fluid, DataComponentPatch.EMPTY);
    }
    
    public static FluidResource of(Fluid fluid, DataComponentPatch components) {
        Objects.requireNonNull(fluid, "Fluid may not be null");
        Objects.requireNonNull(components, "Components may not be null");

        var goodFluid = fluid;

        if (!fluid.isSource(fluid.defaultFluidState()) && !fluid.equals(Fluids.EMPTY)) {
            if (fluid instanceof FlowingFluid flowingFluid) {
                goodFluid = flowingFluid.getSource();
            } else {
                var id = BuiltInRegistries.FLUID.getKey(fluid);
                throw new IllegalArgumentException(String.format(Locale.ROOT, "Can't convert flowing fluid %s (%s) into a still fluid", id.toString(), fluid.toString()));
            }
        }
        
        return new FluidResource(goodFluid, components);
    }

}
