package com.keinloree.world.design;

/** Rejilla 3D de materiales con herramientas de relleno. Los limites son inclusivos. */
public final class Grid {
    public final int x0;
    public final int y0;
    public final int z0;
    public final int nx;
    public final int ny;
    public final int nz;
    private final byte[] cells;

    public Grid(int x0, int x1, int y0, int y1, int z0, int z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.nx = x1 - x0 + 1;
        this.ny = y1 - y0 + 1;
        this.nz = z1 - z0 + 1;
        this.cells = new byte[this.nx * this.ny * this.nz];
    }

    public boolean in(int x, int y, int z) {
        return x >= this.x0 && y >= this.y0 && z >= this.z0
                && x < this.x0 + this.nx && y < this.y0 + this.ny && z < this.z0 + this.nz;
    }

    private int idx(int x, int y, int z) {
        return ((x - this.x0) * this.ny + (y - this.y0)) * this.nz + (z - this.z0);
    }

    public byte get(int x, int y, int z) {
        return this.in(x, y, z) ? this.cells[this.idx(x, y, z)] : Mat.NONE;
    }

    public void set(int x, int y, int z, byte m) {
        if (this.in(x, y, z)) {
            this.cells[this.idx(x, y, z)] = m;
        }
    }

    /** Solo escribe donde todavia no hay nada. */
    public void setIfEmpty(int x, int y, int z, byte m) {
        if (this.in(x, y, z) && this.cells[this.idx(x, y, z)] == Mat.NONE) {
            this.cells[this.idx(x, y, z)] = m;
        }
    }

    public void fill(int ax, int ay, int az, int bx, int by, int bz, byte m) {
        int lx = Math.min(ax, bx);
        int hx = Math.max(ax, bx);
        int ly = Math.min(ay, by);
        int hy = Math.max(ay, by);
        int lz = Math.min(az, bz);
        int hz = Math.max(az, bz);
        for (int x = lx; x <= hx; x++) {
            for (int y = ly; y <= hy; y++) {
                for (int z = lz; z <= hz; z++) {
                    this.set(x, y, z, m);
                }
            }
        }
    }

    /** Cuenta de celdas distintas de NONE. */
    public int count() {
        int n = 0;
        for (byte b : this.cells) {
            if (b != Mat.NONE) {
                n++;
            }
        }
        return n;
    }

    public int maxX() {
        return this.x0 + this.nx - 1;
    }

    public int maxY() {
        return this.y0 + this.ny - 1;
    }

    public int maxZ() {
        return this.z0 + this.nz - 1;
    }

    /** Sala: aire en [ax..bx]x[ay..by]x[az..bz] y una capa de revestimiento alrededor. */
    public void room(int ax, int ay, int az, int bx, int by, int bz, byte floor, byte wall, byte ceil) {
        for (int x = ax - 1; x <= bx + 1; x++) {
            for (int y = ay - 1; y <= by + 1; y++) {
                for (int z = az - 1; z <= bz + 1; z++) {
                    boolean inside = x >= ax && x <= bx && y >= ay && y <= by && z >= az && z <= bz;
                    if (inside) {
                        this.set(x, y, z, Mat.AIR);
                    } else if (y == ay - 1) {
                        this.set(x, y, z, floor);
                    } else if (y == by + 1) {
                        this.set(x, y, z, ceil);
                    } else {
                        this.set(x, y, z, wall);
                    }
                }
            }
        }
    }

    /** Hueco (puerta, pasillo): solo aire. */
    public void carve(int ax, int ay, int az, int bx, int by, int bz) {
        this.fill(ax, ay, az, bx, by, bz, Mat.AIR);
    }
}
