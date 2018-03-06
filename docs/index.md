# Javadocs
You can select one of the below versions to go to the javadoc for that version of GatorGradle.

{% assign pre_sorted_builds = site.data.versions | sort: 'date' %}
{% assign sorted_builds = pre_sorted_builds | reversed %}
<ul>
{% for build in sorted_builds %}
    <li>
        <h4><a href="/gatorgradle/docs/{{ build.name }}">
            Javadoc for {{ build.name }}
        </a></h4>
        <h6>Published on {{ build.date }}</h6>
    </li>
{% endfor%}
</ul>
