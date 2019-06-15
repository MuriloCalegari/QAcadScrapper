package calegari.murilo.qacadscrapper.utils;

import org.threeten.bp.LocalDate;

import java.net.URL;

public class ClassMaterial {
	private String title;
	private URL downloadURL;
	private int subjectId;
	private LocalDate releaseDate;

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
}
