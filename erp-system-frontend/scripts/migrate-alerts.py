import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src' / 'app'

BLOCK_RE = re.compile(
    r'<div\s+class="alert alert-(success|danger|warning|info)(?: alert-dismissible fade show)?(?:\s+(mt-3|mb-3|mb-2))?"\s+\*ngIf="([^"]+)">\s*'
    r'\{\{\s*([^}|]+)(?:\s*\|\s*translate)?\s*\}\}\s*'
    r'(?:<button type="button" class="btn-close" \(click\)="([^"]+)"></button>\s*)?'
    r'</div>',
    re.MULTILINE,
)

NGIF_FIRST_RE = re.compile(
    r'<div\s+\*ngIf="([^"]+)"\s+class="alert alert-(success|danger|warning|info)(?:\s+(mt-3|mb-3|mb-2))?">\s*'
    r'\{\{\s*([^}|]+)(?:\s*\|\s*translate)?\s*\}\}\s*'
    r'(?:<button type="button" class="btn-close" \(click\)="([^"]+)"></button>\s*)?'
    r'</div>',
    re.MULTILINE,
)

PLAIN_RE = re.compile(
    r'<div\s+class="alert alert-(success|danger|warning|info)(?:\s+(mt-3|mb-3|mb-2))?"\s+\*ngIf="([^"]+)">\s*'
    r'\{\{\s*([^}|]+)(?:\s*\|\s*translate)?\s*\}\}\s*</div>',
    re.MULTILINE,
)


def to_component(match, ngif_idx: int, type_idx: int, extra_idx: int, msg_idx: int, click_idx: int | None):
    groups = match.groups()
    ngif = groups[ngif_idx]
    typ = groups[type_idx]
    extra = (groups[extra_idx] or '').strip()
    msg = groups[msg_idx].strip()
    click = groups[click_idx] if click_idx is not None and len(groups) > click_idx else None
    extra_attr = f' extraClass="{extra}"' if extra else ''
    if click:
        dismiss = f' (dismissed)="{click}"'
    elif match.group(0).find('btn-close') >= 0:
        dismiss = f' (dismissed)="{msg} = \'\'"'
    else:
        return (
            f'<app-erp-alert *ngIf="{ngif}" type="{typ}" [message]="{msg}"{extra_attr} '
            f'[dismissible]="false"></app-erp-alert>'
        )
    return f'<app-erp-alert *ngIf="{ngif}" type="{typ}" [message]="{msg}"{extra_attr}{dismiss}></app-erp-alert>'


def migrate_file(path: Path) -> bool:
    text = path.read_text(encoding='utf-8')
    orig = text
    text = BLOCK_RE.sub(lambda m: to_component(m, 2, 0, 1, 3, 4), text)
    text = NGIF_FIRST_RE.sub(lambda m: to_component(m, 0, 1, 2, 3, 4), text)
    text = PLAIN_RE.sub(lambda m: to_component(m, 2, 0, 1, 3, None), text)
    if text != orig:
        path.write_text(text, encoding='utf-8')
        return True
    return False


def main() -> None:
    count = 0
    for path in ROOT.rglob('*.html'):
        if 'erp-alert.component.html' in str(path):
            continue
        if path.parts[-3:-1] == ('theme', 'shared') and path.name == 'alert.component.html':
            continue
        if migrate_file(path):
            print(path.relative_to(ROOT))
            count += 1
    print(f'migrated {count} files')


if __name__ == '__main__':
    main()
