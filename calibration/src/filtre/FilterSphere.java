package filtre;

import common.TypeCalibration;
import data.Vecteur;

import ellipsoide.Sphere;

public class FilterSphere extends Filter {

	private int maxX = 0;
	private int minX = 0;
	private int maxY = 0;
	private int minY = 0;
	private int maxZ = 0;
	private int minZ = 0;
	private Sphere s;
	private int rayon = 0;
	private Vecteur center = new Vecteur(0,0,0);
	
	/**
	 * Creates a filter who plots the vector in a two dimensional window
	 * (simple orthogonal projection on xy)
	 * @param windowSize
	 * @param type
	 */
	public FilterSphere(int windowSize, TypeCalibration type) {
		super(windowSize, type);
		s = new Sphere(20,10);
	}

	/**
	 * Creates a filter who uses the plot given as an argument to plot in
	 * a two dimensional graph (simple orthogonal projection on xy)
	 * @param plot
	 * @param windowSize
	 * @param type
	 */
	public FilterSphere(Sphere sphere, int windowSize, TypeCalibration type) {
		super(windowSize, type);
		this.s = sphere;
	}

	/**
	 * Add the vector given in argument to the filter and update the sphere
	 * with new radius and center
	 * @param v vector to add
	 */
	@Override
	public void add(VecteurFiltrable<Double> v) {
		super.add(v);
		if (v.isCorrect()) {
			if (v.getX() > maxX)
				maxX = (int) v.getX();
			if (v.getY() > maxY)
				maxY = (int) v.getY();
			if (v.getZ() > maxZ)
				maxZ = (int) v.getZ();
			if (v.getX() < minX)
				minX = (int) v.getX();
			if (v.getY() < minY)
				minY = (int) v.getY();
			if (v.getZ() < minZ)
				minZ = (int) v.getZ();
		}
		rayon = (maxX - minX > maxY - minY ? (maxX - minX > maxZ - minZ ? maxX
				- minX : maxZ - minZ) : (maxY - minY > maxZ - minZ ? maxY
				- minY : maxZ - minZ));
		center = new Vecteur((maxX + minX)/2,(maxY + minY)/2,(maxZ + minZ)/2);
		s.update(rayon, center, v);
	}

	
}