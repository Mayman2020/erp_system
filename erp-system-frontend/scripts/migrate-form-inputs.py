import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src' / 'app'

SKIP_PARTS = {
    'theme/layout/admin/configuration',
    'theme/layout/admin/nav-bar/nav-left/nav-search',
}


def should_skip(path: Path) -> bool:
    rel = path.relative_to(ROOT).as_posix()
    return any(part in rel for part in SKIP_PARTS)


def migrate(text: str) -> str:
    text = text.replace('class="form-control form-control-sm"', 'class="erp-input erp-input--sm"')
    text = text.replace("class='form-control form-control-sm'", "class='erp-input erp-input--sm'")
    text = re.sub(r'class="form-control"', 'class="erp-input"', text)
    text = re.sub(r"class='form-control'", "class='erp-input'", text)
    text = re.sub(r'\bclass="form-control\b', 'class="erp-input', text)
    return text


def main() -> None:
    count = 0
    for path in ROOT.rglob('*.html'):
        if should_skip(path):
            continue
        original = path.read_text(encoding='utf-8')
        updated = migrate(original)
        if updated != original:
            path.write_text(updated, encoding='utf-8')
            print(path.relative_to(ROOT))
            count += 1
    print(f'migrated {count} files')


if __name__ == '__main__':
    main()
