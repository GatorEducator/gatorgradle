---
layout: default
---

# Javadocs
You can select one of the below versions to go to the javadoc for that version of GatorGradle.

{% assign sortedBuilds = site.data.versions | sort: 'semantic' %}
{% assign sortedBuilds = sortedBuilds | uniq: 'url' %}
<ul class="version-list">
{% for version in sortedBuilds reversed %}
    <li>
        <a href="{{ site.baseurl }}/{{ version.url }}">
            Javadoc for {{ version.semantic }}
        </a>
        <ul>
            <li>Build {{ version.build }}   —   Published on {{ version.date }}</li>
        </ul>
    </li>
{% endfor%}
</ul>
