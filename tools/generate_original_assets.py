"""Generate the redistributable visual and audio assets used by KlotskiPuzzle.

The script is deterministic and uses only Python's standard library plus Pillow.
No downloaded images, recordings, samples, or game footage are used.
"""

from __future__ import annotations

import math
import random
import struct
import wave
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
IMAGE_ROOT = ROOT / "resources" / "original" / "image"
PIECE_ROOT = IMAGE_ROOT / "pieces"
ICON_ROOT = IMAGE_ROOT / "icons"
MUSIC_ROOT = ROOT / "resources" / "original" / "audio" / "music"
EFFECT_ROOT = ROOT / "resources" / "original" / "audio" / "sound-effect"
DOCS_ROOT = ROOT / "docs" / "assets"
RNG = random.Random(4499)


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = [
        Path("C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc"),
        Path("C:/Windows/Fonts/simhei.ttf"),
        Path("/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc" if bold else
             "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"),
        Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else
             "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def centered_text(draw: ImageDraw.ImageDraw, xy: tuple[int, int], text: str,
                  text_font: ImageFont.ImageFont, fill: tuple[int, ...],
                  anchor: str = "mm") -> None:
    draw.text(xy, text, font=text_font, fill=fill, anchor=anchor)


def vertical_gradient(size: tuple[int, int], top: tuple[int, int, int],
                      bottom: tuple[int, int, int]) -> Image.Image:
    width, height = size
    image = Image.new("RGB", size)
    pixels = image.load()
    for y in range(height):
        ratio = y / max(1, height - 1)
        color = tuple(round(a + (b - a) * ratio) for a, b in zip(top, bottom))
        for x in range(width):
            pixels[x, y] = color
    return image


def add_paper_texture(image: Image.Image, amount: int = 8) -> Image.Image:
    noise = Image.new("L", image.size)
    noise.putdata([128 + RNG.randint(-amount, amount) for _ in range(image.width * image.height)])
    grain = Image.merge("RGB", (noise, noise, noise))
    return Image.blend(image.convert("RGB"), grain, 0.08)


def make_game_background() -> None:
    image = vertical_gradient((1280, 720), (24, 27, 34), (67, 43, 36))
    draw = ImageDraw.Draw(image, "RGBA")
    # Abstract gate and ink arcs: recognisably Eastern without competing with the board.
    draw.arc((360, 72, 920, 690), 194, 346, fill=(224, 181, 105, 28), width=3)
    draw.arc((430, 150, 850, 650), 194, 346, fill=(224, 181, 105, 18), width=2)
    draw.line((378, 284, 378, 670), fill=(224, 181, 105, 18), width=4)
    draw.line((902, 284, 902, 670), fill=(224, 181, 105, 18), width=4)
    for offset, alpha in ((0, 18), (54, 12), (108, 8)):
        points = []
        for x in range(0, 1281, 32):
            y = 560 + offset + 18 * math.sin(x / 120.0) + 8 * math.sin(x / 47.0)
            points.append((x, y))
        draw.line(points, fill=(234, 207, 154, alpha), width=2)
    draw.rounded_rectangle((26, 24, 1254, 696), radius=34,
                           outline=(224, 181, 105, 55), width=2)
    add_paper_texture(image).save(IMAGE_ROOT / "game-background.png", optimize=True)


def make_parchment() -> None:
    image = vertical_gradient((533, 300), (239, 217, 164), (196, 153, 91))
    draw = ImageDraw.Draw(image, "RGBA")
    draw.rounded_rectangle((8, 8, 524, 291), radius=24, outline=(86, 49, 25, 180), width=5)
    for offset in (18, 26):
        draw.rounded_rectangle((offset, offset, 532 - offset, 300 - offset), radius=18,
                               outline=(113, 69, 36, 60), width=2)
    for _ in range(90):
        x = RNG.randrange(20, 513)
        y = RNG.randrange(18, 282)
        r = RNG.choice((1, 1, 2, 3))
        draw.ellipse((x - r, y - r, x + r, y + r), fill=(83, 48, 26, RNG.randrange(8, 28)))
    image.filter(ImageFilter.GaussianBlur(0.25)).save(IMAGE_ROOT / "parchment.png", optimize=True)


def make_login_animation() -> None:
    frames: list[Image.Image] = []
    width, height = 1280, 720
    for frame_index in range(24):
        image = vertical_gradient((width, height), (24, 30, 43), (100, 58, 43))
        draw = ImageDraw.Draw(image, "RGBA")
        moon_x, moon_y = int(width * 0.77), int(height * 0.23)
        moon_radius = 86
        draw.ellipse((moon_x - moon_radius, moon_y - moon_radius,
                      moon_x + moon_radius, moon_y + moon_radius),
                     fill=(246, 218, 157, 208))
        draw.ellipse((moon_x - 64, moon_y - 70, moon_x + 73, moon_y + 68),
                     fill=(255, 236, 190, 46))
        # Slowly drifting layered hills.
        for layer, color in enumerate(((31, 38, 43, 255), (43, 48, 45, 255), (59, 51, 43, 255))):
            base = int(height * 0.64) + layer * 72
            points = [(0, height), (0, base)]
            for x in range(0, width + 100, 100):
                phase = (x / 146.0) + frame_index * (0.012 + layer * 0.004)
                y = base - 50 - 29 * math.sin(phase) - 16 * math.sin(phase * 0.47)
                points.append((x, y))
            points.extend(((width, height), (0, height)))
            draw.polygon(points, fill=color)
        # Original animated motes keep the login screen visibly alive.
        for mote in range(34):
            angle = (mote * 0.73 + frame_index * 0.085) % (2 * math.pi)
            x = (mote * 209 + frame_index * (6 + mote % 3)) % (width + 100) - 50
            y = 105 + (mote * 89) % 495 + 20 * math.sin(angle)
            r = 2 + mote % 3
            draw.ellipse((x - r, y - r, x + r, y + r), fill=(247, 189, 95, 85 + mote % 4 * 30))
        frames.append(image.quantize(colors=128, method=Image.Quantize.MEDIANCUT))
    frames[0].save(
        IMAGE_ROOT / "login-background.gif",
        save_all=True,
        append_images=frames[1:],
        duration=90,
        loop=0,
        optimize=True,
        disposal=2,
    )


def make_piece(path: Path, size: tuple[int, int],
               top: tuple[int, int, int], bottom: tuple[int, int, int],
               accent: tuple[int, int, int]) -> None:
    width, height = size
    image = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(image, "RGBA")
    # Soft depth, lacquer body, and restrained metal inlay replace the old stripe-heavy tiles.
    draw.rounded_rectangle((5, 7, width - 2, height - 1), radius=17,
                           fill=(18, 11, 9, 120))
    body = vertical_gradient((width - 8, height - 10), top, bottom).convert("RGBA")
    body_mask = Image.new("L", body.size, 0)
    ImageDraw.Draw(body_mask).rounded_rectangle((0, 0, body.width - 1, body.height - 1),
                                                radius=15, fill=255)
    image.alpha_composite(Image.composite(body, Image.new("RGBA", body.size), body_mask), (3, 3))
    draw = ImageDraw.Draw(image, "RGBA")
    draw.rounded_rectangle((3, 3, width - 6, height - 8), radius=15,
                           outline=(46, 27, 19, 255), width=3)
    draw.rounded_rectangle((9, 9, width - 12, height - 14), radius=11,
                           outline=accent + (205,), width=2)
    draw.rounded_rectangle((14, 14, width - 17, height - 19), radius=9,
                           outline=(255, 235, 190, 55), width=1)
    # A quiet central seal gives each role a focal point without baking language into the asset.
    seal_radius = min(width, height) // 5
    center_x, center_y = width // 2, height // 2 - 2
    draw.ellipse((center_x - seal_radius, center_y - seal_radius,
                  center_x + seal_radius, center_y + seal_radius),
                 fill=accent + (24,), outline=accent + (76,), width=2)
    local_rng = random.Random(path.name)
    for _ in range(max(5, width * height // 3600)):
        x = local_rng.randint(14, max(14, width - 18))
        y = local_rng.randint(14, max(14, height - 20))
        radius = local_rng.choice((1, 1, 2))
        draw.ellipse((x - radius, y - radius, x + radius, y + radius),
                     fill=(255, 239, 198, local_rng.randint(4, 10)))
    image.save(path, optimize=True)


def make_pieces() -> None:
    make_piece(PIECE_ROOT / "commander.png", (200, 200),
               (151, 57, 48), (78, 28, 27), (229, 180, 96))
    make_piece(PIECE_ROOT / "horizontal-general.png", (200, 100),
               (174, 111, 47), (91, 49, 24), (245, 206, 126))
    vertical_colors = [
        ((55, 116, 104), (27, 70, 65)),
        ((64, 91, 140), (34, 52, 92)),
        ((114, 82, 133), (65, 44, 80)),
        ((136, 91, 55), (77, 49, 29)),
    ]
    for index, colors in enumerate(vertical_colors, 1):
        make_piece(PIECE_ROOT / f"vertical-general-{index}.png", (100, 200),
                   *colors, (229, 192, 118))
    soldier_colors = [
        ((153, 126, 65), (88, 70, 31)),
        ((132, 119, 79), (74, 67, 40)),
        ((147, 105, 72), (86, 57, 37)),
        ((108, 119, 86), (58, 70, 46)),
    ]
    for index, colors in enumerate(soldier_colors, 1):
        make_piece(PIECE_ROOT / f"soldier-{index}.png", (100, 100),
                   *colors, (224, 196, 126))


def make_icon(name: str, symbol: str) -> None:
    image = Image.new("RGBA", (50, 50), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image, "RGBA")
    draw.ellipse((3, 3, 47, 47), fill=(45, 35, 31, 188), outline=(235, 199, 126, 220), width=2)
    color = (255, 235, 192, 245)
    if symbol == "play":
        draw.polygon(((20, 14), (20, 36), (36, 25)), fill=color)
    elif symbol == "pause":
        draw.rounded_rectangle((17, 14, 22, 36), 2, fill=color)
        draw.rounded_rectangle((28, 14, 33, 36), 2, fill=color)
    elif symbol == "previous":
        draw.rectangle((15, 15, 19, 35), fill=color)
        draw.polygon(((34, 14), (34, 36), (19, 25)), fill=color)
    elif symbol == "next":
        draw.rectangle((31, 15, 35, 35), fill=color)
        draw.polygon(((16, 14), (16, 36), (31, 25)), fill=color)
    image.save(ICON_ROOT / name, optimize=True)


def board_preview(size: tuple[int, int], *, title: bool = True) -> Image.Image:
    width, height = size
    image = vertical_gradient(size, (27, 31, 40), (95, 57, 40)).convert("RGBA")
    draw = ImageDraw.Draw(image, "RGBA")
    if title:
        centered_text(draw, (width // 2, 64), "KlotskiPuzzle", font(48, bold=True), (255, 239, 207, 255))
        centered_text(draw, (width // 2, 110), "Java 22+  •  Swing  •  A* Solver", font(22),
                      (226, 204, 164, 235))
    board_height = min(height - (150 if title else 50), 390)
    cell = board_height // 5
    board_width = cell * 4
    origin_x = width // 2 - board_width // 2
    origin_y = (145 if title else 25) + max(0, (height - (145 if title else 25) - board_height) // 2)
    draw.rounded_rectangle((origin_x - 14, origin_y - 14, origin_x + board_width + 14,
                            origin_y + board_height + 14), radius=18,
                           fill=(30, 25, 23, 210), outline=(232, 193, 122, 180), width=3)
    pieces = [
        (0, 0, 1, 2, "纵1", (48, 104, 94)), (1, 0, 2, 2, "主将", (137, 53, 43)),
        (3, 0, 1, 2, "纵2", (58, 80, 126)), (0, 2, 1, 2, "纵3", (101, 72, 120)),
        (1, 2, 2, 1, "横将", (150, 91, 38)), (3, 2, 1, 2, "纵4", (124, 78, 45)),
        (0, 4, 1, 1, "兵1", (137, 111, 57)), (3, 4, 1, 1, "兵2", (116, 104, 70)),
    ]
    for col, row, piece_width, piece_height, label, color in pieces:
        x0 = origin_x + col * cell + 3
        y0 = origin_y + row * cell + 3
        x1 = x0 + piece_width * cell - 6
        y1 = y0 + piece_height * cell - 6
        draw.rounded_rectangle((x0, y0, x1, y1), radius=10, fill=color + (255,),
                               outline=(255, 231, 178, 180), width=2)
        centered_text(draw, ((x0 + x1) // 2, (y0 + y1) // 2), label,
                      font(max(14, cell // 4), bold=True), (255, 244, 213, 255))
    return image


def make_document_images() -> None:
    preview = board_preview((900, 560))
    preview.convert("RGB").save(DOCS_ROOT / "project-preview.png", optimize=True)

    social = vertical_gradient((1280, 640), (25, 29, 38), (94, 55, 39)).convert("RGBA")
    draw = ImageDraw.Draw(social, "RGBA")
    centered_text(draw, (390, 190), "KlotskiPuzzle", font(64, bold=True), (255, 239, 205, 255))
    centered_text(draw, (390, 260), "Java 22+ Swing Klotski", font(30), (230, 205, 161, 255))
    centered_text(draw, (390, 310), "A* solver • animated playback • tests", font(24),
                  (215, 190, 151, 235))
    miniature = board_preview((420, 520), title=False).resize((380, 470), Image.Resampling.LANCZOS)
    social.alpha_composite(miniature, (820, 84))
    draw.rounded_rectangle((70, 405, 710, 515), radius=24, fill=(26, 22, 22, 100),
                           outline=(234, 194, 122, 80), width=2)
    centered_text(draw, (390, 452), "Runnable • Testable • Extendable", font(25, bold=True),
                  (255, 239, 205, 255))
    centered_text(draw, (390, 490), "github.com/44-99/KlotskiPuzzle", font(20),
                  (221, 197, 157, 235))
    social.convert("RGB").save(DOCS_ROOT / "social-preview.png", optimize=True)

    for demo_name, english in (("demo.gif", False), ("demo-en.gif", True)):
        demo_frames: list[Image.Image] = []
        title = "A* Search & EDT Playback" if english else "A* 搜索与 EDT 动画回放"
        labels = ("V1", "CAO CAO", "V2", "V3", "V4", "S1", "S2", "H") if english else (
            "纵1", "主将", "纵2", "纵3", "纵4", "兵1", "兵2", "横将")
        for frame_index in range(24):
            frame = vertical_gradient((720, 450), (27, 31, 40), (95, 57, 40)).convert("RGBA")
            overlay = ImageDraw.Draw(frame, "RGBA")
            centered_text(overlay, (360, 40), title, font(28, bold=True),
                          (255, 239, 207, 255))
            cell = 62
            origin_x, origin_y = 230, 78
            overlay.rounded_rectangle((origin_x - 10, origin_y - 10,
                                       origin_x + cell * 4 + 10, origin_y + cell * 5 + 10),
                                      radius=16, fill=(27, 23, 21, 220),
                                      outline=(233, 194, 122, 190), width=3)
            static_pieces = [
                (0, 0, 1, 2, labels[0], (48, 104, 94)),
                (1, 0, 2, 2, labels[1], (137, 53, 43)),
                (3, 0, 1, 2, labels[2], (58, 80, 126)),
                (0, 2, 1, 2, labels[3], (101, 72, 120)),
                (3, 2, 1, 2, labels[4], (124, 78, 45)),
                (0, 4, 1, 1, labels[5], (137, 111, 57)),
                (3, 4, 1, 1, labels[6], (116, 104, 70)),
            ]
            if frame_index < 6:
                move_progress = 0.0
                status = "SwingWorker: searching" if english else "SwingWorker：搜索中"
            elif frame_index < 13:
                move_progress = (frame_index - 6) / 6
                status = "Swing Timer: replaying a legal move" if english else "Swing Timer：回放合法移动"
            elif frame_index < 18:
                move_progress = 1.0
                status = "BoardRules: state updated" if english else "BoardRules：状态已更新"
            else:
                move_progress = 1 - (frame_index - 18) / 5
                status = "Cancellable and replayable" if english else "可取消、可重放"
            pieces = static_pieces + [(1, 2 + move_progress, 2, 1, labels[7], (150, 91, 38))]
            for col, row, piece_width, piece_height, label, color in pieces:
                x0 = origin_x + col * cell + 3
                y0 = origin_y + row * cell + 3
                x1 = x0 + piece_width * cell - 6
                y1 = y0 + piece_height * cell - 6
                overlay.rounded_rectangle((x0, y0, x1, y1), radius=9, fill=color + (255,),
                                          outline=(255, 231, 178, 180), width=2)
                centered_text(overlay, ((x0 + x1) // 2, (y0 + y1) // 2), label,
                              font(15 if english else 17, bold=True), (255, 244, 213, 255))
            overlay.rounded_rectangle((28, 168, 198, 276), radius=18, fill=(24, 22, 22, 125),
                                      outline=(234, 194, 122, 75), width=2)
            centered_text(overlay, (113, 202), "1  Background A*" if english else "1  后台求解",
                          font(17 if english else 19, bold=True), (255, 239, 207, 255))
            centered_text(overlay, (113, 239), "2  EDT playback" if english else "2  EDT 回放",
                          font(17 if english else 19, bold=True), (255, 239, 207, 255))
            centered_text(overlay, (360, 422), status, font(18), (232, 208, 168, 255))
            demo_frames.append(frame.convert("RGB").quantize(colors=128))
        demo_frames[0].save(DOCS_ROOT / demo_name, save_all=True, append_images=demo_frames[1:],
                            duration=110, loop=0, optimize=True, disposal=2)


def clamp_sample(value: float) -> int:
    return max(-32767, min(32767, int(value * 32767)))


def write_wav(path: Path, samples: list[float], rate: int = 22_050) -> None:
    with wave.open(str(path), "wb") as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(rate)
        stream.writeframes(b"".join(struct.pack("<h", clamp_sample(sample)) for sample in samples))


def envelope(position: int, note_samples: int, attack: float = 0.08, release: float = 0.18) -> float:
    ratio = position / max(1, note_samples - 1)
    if ratio < attack:
        return ratio / attack
    if ratio > 1 - release:
        return max(0.0, (1 - ratio) / release)
    return 1.0


def make_track(path: Path, notes: list[int | None], bpm: int, timbre: str,
               chord_roots: list[int]) -> None:
    rate = 22_050
    step_samples = int(rate * 60 / bpm / 2)
    samples: list[float] = []
    for step in range(64):
        midi = notes[step % len(notes)]
        root = chord_roots[(step // 8) % len(chord_roots)]
        root_frequency = 440.0 * (2 ** ((root - 69) / 12))
        fifth_frequency = root_frequency * 1.5
        frequency = 0.0 if midi is None else 440.0 * (2 ** ((midi - 69) / 12))
        for index in range(step_samples):
            time = index / rate
            lead = 0.0
            if midi is not None:
                phase = math.tau * frequency * time
                if timbre == "plucked":
                    lead = (math.sin(phase) + 0.34 * math.sin(phase * 2)
                            + 0.14 * math.sin(phase * 3))
                    lead *= math.exp(-5.2 * index / step_samples)
                elif timbre == "flute":
                    vibrato = 1 + 0.0035 * math.sin(math.tau * 5.1 * time)
                    lead = math.sin(phase * vibrato) + 0.12 * math.sin(phase * 2)
                elif timbre == "bell":
                    lead = (math.sin(phase) + 0.28 * math.sin(phase * 2.01)
                            + 0.11 * math.sin(phase * 3.97))
                    lead *= math.exp(-3.7 * index / step_samples)
                else:
                    lead = math.sin(phase) + 0.16 * math.sin(phase * 0.5)
                lead *= envelope(index, step_samples, attack=0.06, release=0.28)
            pad_envelope = 0.72 + 0.28 * math.sin(math.pi * index / step_samples)
            pad = (math.sin(math.tau * root_frequency * time) * 0.7
                   + math.sin(math.tau * fifth_frequency * time) * 0.3) * pad_envelope
            pulse = 0.0
            if step % 4 == 0:
                pulse = math.sin(math.tau * (root_frequency / 2) * time)
                pulse *= math.exp(-7.0 * index / step_samples)
            samples.append(lead * 0.105 + pad * 0.025 + pulse * 0.045)

    # Two restrained echoes create space without relying on third-party samples.
    for delay_seconds, gain in ((0.18, 0.16), (0.34, 0.08)):
        delay = int(rate * delay_seconds)
        dry = samples.copy()
        for index in range(delay, len(samples)):
            samples[index] += dry[index - delay] * gain
    fade = min(rate // 8, len(samples) // 2)
    for index in range(fade):
        factor = index / fade
        samples[index] *= factor
        samples[-index - 1] *= factor
    write_wav(path, samples, rate)


def make_effect(path: Path, frequencies: tuple[float, ...], duration: float,
                sweep: float = 0.0, noise: float = 0.0) -> None:
    rate = 22_050
    total = int(rate * duration)
    local_rng = random.Random(path.name)
    samples: list[float] = []
    for index in range(total):
        time = index / rate
        ratio = index / max(1, total - 1)
        fade = math.sin(math.pi * ratio) ** 1.6
        value = 0.0
        for harmonic, frequency in enumerate(frequencies, 1):
            current = frequency * (1 + sweep * ratio)
            value += math.sin(math.tau * current * time) / harmonic
        value = value / max(1, len(frequencies))
        value += noise * (local_rng.random() * 2 - 1)
        samples.append(value * fade * 0.42)
    write_wav(path, samples, rate)


def make_audio() -> None:
    tracks = [
        ("dawn-path.wav", [62, 65, 69, 72, 69, 65, 64, None,
                           62, 64, 65, 69, 72, 69, 65, None], 96, "flute", [50, 46, 48, 43]),
        ("woodland-steps.wav", [57, 60, 64, 67, 64, 60, 59, 60,
                               57, 59, 60, 64, 67, 69, 67, None], 108, "plucked", [45, 41, 43, 40]),
        ("quiet-strategy.wav", [60, None, 64, 67, None, 69, 67, 64,
                               62, None, 60, 57, 60, 62, 64, None], 84, "warm", [48, 45, 41, 43]),
        ("open-gate.wav", [65, 69, 72, 74, 72, 69, 67, 65,
                          69, 72, 74, 77, 74, 72, 69, None], 112, "bell", [53, 48, 50, 46]),
    ]
    for name, notes, bpm, timbre, chord_roots in tracks:
        make_track(MUSIC_ROOT / name, notes, bpm, timbre, chord_roots)
    make_effect(EFFECT_ROOT / "move.wav", (196, 294), 0.13, sweep=-0.18, noise=0.11)
    make_effect(EFFECT_ROOT / "select.wav", (660, 880), 0.09, sweep=0.08)
    make_effect(EFFECT_ROOT / "invalid.wav", (145, 116), 0.15, sweep=-0.30, noise=0.16)
    make_effect(EFFECT_ROOT / "undo.wav", (330, 247, 196), 0.24, sweep=-0.35, noise=0.035)
    make_effect(EFFECT_ROOT / "victory.wav", (523.25, 659.25, 783.99), 0.92, sweep=0.05)
    make_effect(EFFECT_ROOT / "defeat.wav", (196.0, 146.83), 0.85, sweep=-0.42, noise=0.025)


def main() -> None:
    for directory in (IMAGE_ROOT, PIECE_ROOT, ICON_ROOT, MUSIC_ROOT, EFFECT_ROOT, DOCS_ROOT):
        directory.mkdir(parents=True, exist_ok=True)
    make_game_background()
    make_parchment()
    make_login_animation()
    make_pieces()
    make_icon("play.png", "play")
    make_icon("pause.png", "pause")
    make_icon("previous.png", "previous")
    make_icon("next.png", "next")
    make_document_images()
    make_audio()
    print("Generated original visual and audio assets under resources/original and docs/assets.")


if __name__ == "__main__":
    main()
