# Android 资源命名规范

## 适用范围

本规范适用于项目内各 Android module 及 pins 分包中的新增资源。

旧资源不要求一次性重命名；修改或迁移旧资源时，可根据实际影响逐步适配。

## 基本原则

- 资源名称使用小写字母和下划线。
- 资源应添加所属业务或组件的短前缀，避免多个 module 合并资源时发生重名。
- 名称应表达资源的场景、对象和状态，避免含义不明确的单字母缩写。
- `button` 统一简写为 `btn`。
- `background` 统一简写为 `bg`。
- Style 使用大驼峰命名，不强制添加小写资源前缀。
- 尺寸直接在布局中使用，不要求提取到 `dimens.xml` 复用。

## 通用格式

文件和普通资源：

```text
<业务前缀>_<场景>_<对象>_<状态>
```

View ID：

```text
<业务前缀>_<控件类型>_<场景>_<语义>
```

状态名称统一使用完整单词：

```text
normal
pressed
disabled
selected
checked
```

不要使用 `n`、`p`、`un_enable`、`enable_false` 等不明确或不统一的状态名称。

## CommonUseDialog 示例

`CommonUseDialog` 使用 `cu_` 作为资源前缀。

### Layout

布局名称应表达组件和布局方向：

```text
cu_common_dialog_horizontal.xml
cu_common_dialog_vertical.xml
```

### Drawable

背景及状态资源示例：

```text
cu_dialog_bg.xml
cu_cancel_btn_bg_normal.xml
cu_cancel_btn_bg_pressed.xml
cu_cancel_btn_bg_selector.xml
cu_confirm_btn_bg_normal.xml
cu_confirm_btn_bg_pressed.xml
cu_confirm_btn_bg_disabled.xml
cu_confirm_btn_bg_selector.xml
```

### Color

颜色名称应包含用途和状态：

```text
cu_text
cu_confirm_text
cu_confirm_normal
cu_confirm_pressed
cu_confirm_disabled
cu_cancel_normal
cu_cancel_pressed
```

### String

字符串名称应包含业务前缀和具体语义：

```text
cu_common_cancel
cu_common_confirm
```

### View ID

所有 View ID 都应包含业务前缀。

控件类型缩写：

```text
tv  TextView
iv  ImageView
ll  LinearLayout
rl  RelativeLayout
```

示例：

```text
rl_dialog_root
tv_dialog_title
tv_dialog_message
tv_dialog_confirm
tv_dialog_cancel
ll_dialog_center_group
ll_dialog_btn_group
iv_dialog_close
```

### Style

Style 定义放在 `res/values/styles.xml`，使用大驼峰命名。

`CommonUseDialog` 的 Style 名称保持为：

```text
CommonUseDialog
```

### 图片目录

普通界面图片放在 `drawable` 或相应密度的 `drawable-*` 目录，不放在 `mipmap`：

```text
drawable-xhdpi/cu_dialog_card_close.png
```

`mipmap` 目录主要用于应用启动图标。

## 现有资源处理

- 本规范优先约束新增资源。
- 不因引入本规范而批量修改现有资源。
- 重命名现有资源前，需要确认所有 Java、Kotlin、XML 和其他 module 中的引用。
