# Warn when there is a big PR
warn("Big PR") if git.lines_of_code > 1000

# Code coverage
jacoco.minimum_project_coverage_percentage = 80
jacoco.report("sdk-utils/build/reports/jacoco/jacocoReleaseReport/jacocoReleaseReport.xml", fail_no_coverage_data_found: false)