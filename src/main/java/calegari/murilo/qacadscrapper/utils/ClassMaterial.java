package calegari.murilo.qacadscrapper.utils;

import org.threeten.bp.LocalDate;

import java.net.URL;

public class ClassMaterial {
	private int id;
	private String title;
	private URL downloadURL;
	private int subjectId;
	private LocalDate releaseDate;

	public ClassMaterial() {}

	public ClassMaterial(int id, String title, URL downloadURL, LocalDate releaseDate, int subjectId) {
		this.id = id;
		this.title = title;
		this.downloadURL = downloadURL;
		this.subjectId = subjectId;
		this.releaseDate = releaseDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public URL getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(URL downloadURL) {
		this.downloadURL = downloadURL;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) { // If it's the same instance
			return true;
		}

		if(obj instanceof ClassMaterial) {
			ClassMaterial classMaterial = (ClassMaterial) obj;
			return this.getSubjectId() == classMaterial.getSubjectId() &&
					this.getTitle().equals(classMaterial.getTitle()) &&
					this.getDownloadURL().equals(classMaterial.getDownloadURL()) &&
					this.getReleaseDate().equals(classMaterial.getReleaseDate());
		}

		return false;
	}
}
