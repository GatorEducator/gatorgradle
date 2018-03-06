# Javadocs
You can select one of the below versions to go to the javadoc for that version of GatorGradle.

{% assign sortedBuilds = (site.data.versions | sort: 'id') | reverse %}
<ul>
{% for build in sortedBuilds %}
    <li>
        <h4><a href="/gatorgradle/docs/{{ build.name }}">
            Javadoc for {{ build.name }}
        </a></h4>
        <h6>Published on {{ build.date }}</h6>
    </li>
{% endfor%}
</ul>
