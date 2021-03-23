package com.github.rising3.gradle.semver

/**
 * Semantic Versioning.
 *
 * @auther rising3.
 */
class SemVer {
	/**
	 * Default pre-release number.
	 */
	private static final int DEFAULT_PRERELEASE_NO = 1

	/**
	 * major.
	 */
	final int major

	/**
	 * minor.
	 */
	final int minor

	/**
	 * patch.
	 */
	final int patch

	/**
	 * pre-identifier.
	 */
	final String preid

	/**
	 * pre-release.
	 */
	final Integer prerelease

	/**
	 * Constructor.
	 */
	def SemVer() {
	}

	/**
	 * Constructor.
	 *
	 * @param major major
	 * @param minor minor
	 * @param patch patch
	 */
	def SemVer(int major, int minor, int patch) {
		this(major, minor, patch, null, null)
	}

	/**
	 * Constructor.
	 *
	 * @param major major
	 * @param minor minor
	 * @param patch patch
	 * @param preid pre-identifier
	 * @param prerelease prerelease
	 */
	def SemVer(int major, int minor, int patch, String preid, Integer prerelease) {
		if (prerelease == null) {
			assert preid == null && prerelease == null
		}
		this.major = major
		this.minor = minor
		this.patch = patch
		this.preid = preid
		this.prerelease = prerelease
	}

	/**
	 * Get current version string.
	 *
	 * @return current version string
	 */
	def String getCurrent() {
		String.format('%d.%d.%d%s%s%s',
				major,
				minor,
				patch,
				prerelease != null ? '-' : '',
				preid != null ? "$preid.": '',
				prerelease != null ? "$prerelease" : '')
	}

	/**
	 * Inclement major.
	 *
	 * @return SemVer
	 */
	def SemVer incMajor() {
		new SemVer(major + 1, 0, 0)
	}

	/**
	 * Inclement minor.
	 *
	 * @return SemVer
	 */
	def SemVer incMinor() {
		new SemVer(major, minor + 1, 0)
	}

	/**
	 * Inclement patch.
	 *
	 * @return SemVer
	 */
	def SemVer incPatch() {
		new SemVer(major, minor, patch + 1)
	}

	/**
	 * Inclement major.
	 *
	 * @param preid pre-identifier
	 * @return SemVer
	 */
	def SemVer incPremajor(String preid) {
		def prerelease = calcPrerelease(preid)
		new SemVer(major + 1, 0, 0, preid, prerelease)
	}

	/**
	 * Inclement minor.
	 *
	 * @param preid pre-identifier
	 * @return SemVer
	 */
	def SemVer incPreminor(String preid) {
		def prerelease = calcPrerelease(preid)
		new SemVer(major, minor + 1, 0, preid, prerelease)
	}

	/**
	 * Inclement patch.
	 *
	 * @param preid pre-identifier
	 * @return SemVer
	 */
	def SemVer incPrepatch(String preid) {
		def prerelease = calcPrerelease(preid)
		new SemVer(major, minor, patch + 1, preid, prerelease)
	}

	/**
	 * Inclement pre-release.
	 *
	 * @param preid pre-identifier
	 * @return SemVer
	 */
	def SemVer incPrerelease(String preid) {
		def prerelease = calcPrerelease(preid)
		new SemVer(major, minor, patch, preid, this.preid == preid ? prerelease + 1 : DEFAULT_PRERELEASE_NO)
	}

	@Override
	String toString() {
		return current
	}

	/**
	 * calculate pre-release no.
	 *
	 * @param preid pre-identifier
	 * @return pre-release no.
	 */
	private Integer calcPrerelease(String preid) {
		def wk = this.preid == preid ? this.prerelease : null
		wk == null ? DEFAULT_PRERELEASE_NO : wk
	}

	/**
	 * Parse semantic versioning string.
	 *
	 * @param s semantic versioning string
	 * @return SemVer
	 */
	static SemVer parse(String s)	{
		assert s

		def p = [
				s.find(/^\d{1,}\.\d{1,}\.\d{1,}\-\w{1,}\.\d{1,}$/),
				s.find(/^\d{1,}\.\d{1,}\.\d{1,}\-\d{1,}$/),
				s.find(/^\d{1,}\.\d{1,}\.\d{1,}$/),
		]

		if (p[0]) {
			def v = p[0].replaceAll(/\-/, '.').split(/\./)
			new SemVer(v[0].toInteger(), v[1].toInteger(), v[2].toInteger(), v[3], v[4].toInteger())
		} else if (p[1]) {
			def v = p[1].replaceAll(/\-/, '.').split(/\./)
			new SemVer(v[0].toInteger(), v[1].toInteger(), v[2].toInteger(), null, v[3].toInteger())
		} else if (p[2]) {
			def v = p[2].split(/\./)
			new SemVer(v[0].toInteger(), v[1].toInteger(), v[2].toInteger())
		} else {
			null
		}
	}
}
