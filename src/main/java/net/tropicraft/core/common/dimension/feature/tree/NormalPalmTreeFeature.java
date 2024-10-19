package net.tropicraft.core.common.dimension.feature.tree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import java.util.Set;
import java.util.function.BiConsumer;

import static net.tropicraft.core.common.dimension.feature.TropicraftFeatureUtil.goesBeyondWorldSize;
import static net.tropicraft.core.common.dimension.feature.TropicraftFeatureUtil.isBBAvailable;

public class NormalPalmTreeFeature extends PalmTreeFeature {
    public NormalPalmTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();

        pos = pos.immutable();

        int height = random.nextInt(4) + 6;

        if (goesBeyondWorldSize(world, pos.getY(), height)) {
            return false;
        }

        if (!isBBAvailable(world, pos, height)) {
            return false;
        }

        if (!getSapling().defaultBlockState().canSurvive(world, pos)) {
            return false;
        }

        if (world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            world.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 3);
        }

        Set<BlockPos> logs = Sets.newHashSet();
        Set<BlockPos> leaves = Sets.newHashSet();

        int i = pos.getX(), j = pos.getY(), k = pos.getZ();

        placeLeaf(leaves, world, i, j + height + 2, k);
        placeLeaf(leaves, world, i, j + height + 1, k + 1);
        placeLeaf(leaves, world, i, j + height + 1, k + 2);
        placeLeaf(leaves, world, i, j + height + 1, k + 3);
        placeLeaf(leaves, world, i, j + height, k + 4);
        placeLeaf(leaves, world, i + 1, j + height + 1, k);
        placeLeaf(leaves, world, i + 2, j + height + 1, k);
        placeLeaf(leaves, world, i + 3, j + height + 1, k);
        placeLeaf(leaves, world, i + 4, j + height, k);
        placeLeaf(leaves, world, i, j + height + 1, k - 1);
        placeLeaf(leaves, world, i, j + height + 1, k - 2);
        placeLeaf(leaves, world, i, j + height + 1, k - 3);
        placeLeaf(leaves, world, i, j + height, k - 4);
        placeLeaf(leaves, world, i - 1, j + height + 1, k);
        placeLeaf(leaves, world, i - 1, j + height + 1, k - 1);
        placeLeaf(leaves, world, i - 1, j + height + 1, k + 1);
        placeLeaf(leaves, world, i + 1, j + height + 1, k - 1);
        placeLeaf(leaves, world, i + 1, j + height + 1, k + 1);
        placeLeaf(leaves, world, i - 2, j + height + 1, k);
        placeLeaf(leaves, world, i - 3, j + height + 1, k);
        placeLeaf(leaves, world, i - 4, j + height, k);
        placeLeaf(leaves, world, i + 2, j + height + 1, k + 2);
        placeLeaf(leaves, world, i + 2, j + height + 1, k - 2);
        placeLeaf(leaves, world, i - 2, j + height + 1, k + 2);
        placeLeaf(leaves, world, i - 2, j + height + 1, k - 2);
        placeLeaf(leaves, world, i + 3, j + height, k + 3);
        placeLeaf(leaves, world, i + 3, j + height, k - 3);
        placeLeaf(leaves, world, i - 3, j + height, k + 3);
        placeLeaf(leaves, world, i - 3, j + height, k - 3);

        for (int j1 = 0; j1 < height + 2; j1++) {
            BlockPos logPos = pos.above(j1);
            if (TreeFeature.validTreePos(world, logPos)) {
                placeLog(logs, world, logPos);
            }
        }

        spawnCoconuts(world, new BlockPos(i, j + height, k), random, 2, getLeaf());

        return BoundingBox.encapsulatingPositions(Iterables.concat(logs, leaves)).map((box) -> {
            DiscreteVoxelShape discretevoxelshape = PalmTreeFeature.updateLeaves(world, box, logs);
            StructureTemplate.updateShapeAtEdge(world, 3, discretevoxelshape, box.minX(), box.minY(), box.minZ());
            return true;
        }).orElse(false);
    }
}
