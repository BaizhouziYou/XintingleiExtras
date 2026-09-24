package cn.xintinglei.content;

import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/** Simple hit shapes intentionally omit the renderer's decorative strips and bevels. */
final class MooncakeShapes {
    private static final VoxelShape[][] SHAPES = load();

    static VoxelShape get(MooncakeBlock.Flavor flavor, int servings) {
        return SHAPES[flavor.ordinal()][servings];
    }

    private static VoxelShape[][] load() {
        VoxelShape[][] shapes = new VoxelShape[MooncakeBlock.Flavor.values().length][5];
        for (var flavor : MooncakeBlock.Flavor.values()) {
            double radius = switch (flavor) {
                case SWEET_BERRY -> 5.8;
                case HONEY -> 5.4;
                default -> 6.0;
            };
            double height = switch (flavor) {
                case SWEET_BERRY -> 5.2;
                case HONEY -> 3.5;
                case SNOW_SKIN -> 3.0;
                default -> 3.8;
            };
            for (int n = 0; n <= 4; n++) {
                if (flavor == MooncakeBlock.Flavor.SWEET_BERRY) {
                    shapes[flavor.ordinal()][n] = hamShoulder(n);
                    continue;
                }
                VoxelShape shape = VoxelShapes.empty();
                // Three broad bands give a rounded outline without a forest of edges.
                for (int band = 0; band < 3; band++) {
                    double z1 = 8 - radius + band * radius * 2 / 3;
                    double z2 = 8 - radius + (band + 1) * radius * 2 / 3;
                    double half = radius * (band == 1 ? 1 : .82);
                    for (int q = 0; q < (n == 0 ? 1 : n); q++) {
                        double x1 = 8 - half, x2 = 8 + half, a = z1, b = z2;
                        if (n > 0) {
                            boolean left = q == 0 || q == 2, north = q < 2;
                            x1 = Math.max(x1, left ? 2 : 8.2);
                            x2 = Math.min(x2, left ? 7.8 : 14);
                            a = Math.max(a, north ? 2 : 8.2);
                            b = Math.min(b, north ? 7.8 : 14);
                        }
                        if (x1 < x2 && a < b) shape = VoxelShapes.union(shape,
                            Block.createCuboidShape(x1, 0, a, x2, height, b));
                    }
                }
                shapes[flavor.ordinal()][n] = shape.simplify();
            }
        }
        return shapes;
    }

    /** Broad targeting bands follow the reference pastry's shallow stepped shoulder. */
    private static VoxelShape hamShoulder(int servings) {
        VoxelShape shape = VoxelShapes.empty();
        double[][] courses = {{0,.3,5.45},{.3,2.6,5.8},{2.6,3.25,5.65},
            {3.25,3.9,5.35},{3.9,4.45,4.95},{4.45,4.9,4.4},{4.9,5.2,3.75}};
        for (double[] course : courses) {
            double radius = course[2];
            for (int band = 0; band < 3; band++) {
                double a = 8 - radius + band * radius * 2 / 3;
                double b = 8 - radius + (band + 1) * radius * 2 / 3;
                double half = radius * (band == 1 ? 1 : .82);
                for (int q = 0; q < (servings == 0 ? 1 : servings); q++) {
                    double x1 = 8 - half, x2 = 8 + half, z1 = a, z2 = b;
                    if (servings > 0) {
                        boolean left = q == 0 || q == 2, north = q < 2;
                        x1 = Math.max(x1, left ? 2 : 8.2);
                        x2 = Math.min(x2, left ? 7.8 : 14);
                        z1 = Math.max(z1, north ? 2 : 8.2);
                        z2 = Math.min(z2, north ? 7.8 : 14);
                    }
                    if (x1 < x2 && z1 < z2) shape = VoxelShapes.union(shape,
                        Block.createCuboidShape(x1, course[0], z1, x2, course[1], z2));
                }
            }
        }
        return shape.simplify();
    }
}
