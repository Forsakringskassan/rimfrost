Run the bash command below exactly as written and display the output as a markdown table. Do not paraphrase or reformat — print the raw table from stdout.

After displaying the table, handle two cases — present them together if both apply:

**SNAPSHOT versions:** If any rows have a POM version ending in `-SNAPSHOT` and there is a stable (non-SNAPSHOT) version available, warn the user that those dependencies are pinned to a SNAPSHOT and ask (numbered list) whether they want to upgrade them to the latest stable version.

**Outdated versions:** If any rows have status `UPDATE AVAILABLE`, ask the user (using a numbered list) whether they want to upgrade all outdated dependencies to their latest versions.

If the user says yes to either (or both):

- Check the current branch with `git branch --show-current`.
- If on `main`, suggest a branch name (e.g. `fix/bump-rimfrost-dependencies`) and ask (numbered list) whether they want to create and switch to that branch before applying updates, or apply directly on `main`.
- Create and switch to the branch if the user chose to do so.

Then update the versions in `pom.xml` accordingly, run `mvn test`, and report the result.

After reporting the test result, present any follow-up actions or questions as a numbered list so the user can reply with just a number.

```bash
python3 << 'PYEOF'
import subprocess, json, re, sys
import xml.etree.ElementTree as ET

ORG = 'forsakringskassan'

def parse_version(v):
    try:
        return tuple(int(x) for x in re.split(r'[.\-]', v) if x.isdigit())
    except Exception:
        return (0,)

tree = ET.parse('pom.xml')
root = tree.getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}

entries = []
for dep in root.findall('.//m:dependency', ns):
    group = (dep.findtext('m:groupId', '', ns) or '').strip()
    artifact = (dep.findtext('m:artifactId', '', ns) or '').strip()
    version = (dep.findtext('m:version', '', ns) or '').strip()
    if 'rimfrost' in group and version:
        entries.append((artifact, group, version))

col_name = max(len(e[0]) for e in entries)
col_ver  = max(len(e[2]) for e in entries)
col_name = max(col_name, 8)
col_ver  = max(col_ver, 10)

header = f"| {'Artifact':<{col_name}} | {'POM Version':<{col_ver}} | Status           | Available Versions (latest first) |"
sep    = f"|{'-'*(col_name+2)}|{'-'*(col_ver+2)}|------------------|-----------------------------------|"
print(header)
print(sep)

for artifact, group, pom_version in entries:
    pkg_name = f'{group}.{artifact}'

    result = subprocess.run(
        ['gh', 'api', f'/orgs/{ORG}/packages/maven/{pkg_name}/versions',
         '--paginate', '--jq', '[.[].name] | unique'],
        capture_output=True, text=True
    )

    if result.returncode == 0 and result.stdout.strip():
        all_versions = []
        for line in result.stdout.strip().splitlines():
            line = line.strip()
            if line.startswith('['):
                try:
                    all_versions.extend(json.loads(line))
                except Exception:
                    pass
        all_versions = [v for v in all_versions if re.match(r'^\d+\.\d+', v)]
        all_versions = sorted(set(all_versions), key=parse_version, reverse=True)
        versions_str = ', '.join(all_versions) if all_versions else '(none)'
        latest = all_versions[0] if all_versions else None
        is_snapshot = pom_version.endswith('-SNAPSHOT')
        has_stable = any(not v.endswith('-SNAPSHOT') for v in all_versions)
        if latest and parse_version(latest) > parse_version(pom_version):
            status = 'UPDATE AVAILABLE'
        elif is_snapshot and has_stable:
            status = 'SNAPSHOT'
        else:
            status = 'OK'
    elif result.returncode != 0:
        versions_str = f'error: {result.stderr.strip()[:60]}'
        status = 'error'
    else:
        versions_str = '(none)'
        status = 'OK'

    print(f"| {artifact:<{col_name}} | {pom_version:<{col_ver}} | {status:<16} | {versions_str} |")
PYEOF
```
