---
layout: default
---

# Javadocs
You can select one of the below versions to go to the javadoc for that version of GatorGradle.

{% assign sortedBuilds = (site.data.versions | sort: 'id') | reverse %}
<ul>
{% for build in sortedBuilds %}
    <li>
        <a href="/gatorgradle/docs/{{ build.name }}">
            Javadoc for {{ build.name }}
        </a>
        <ul><li>Published on {{ build.date }}</li></ul>
    </li>
{% endfor%}
</ul>
