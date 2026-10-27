const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

async function removeOuterWhiteBg(srcPath, destPath, threshold = 238, removeShadows = true) {
  console.log(`Processing: ${path.basename(srcPath)} -> ${path.basename(destPath)}`);
  const image = await Jimp.read(srcPath);
  const width = image.bitmap.width;
  const height = image.bitmap.height;

  const data = image.bitmap.data;
  const visited = new Uint8Array(width * height);
  const queue = [];

  function isBackground(idx) {
    const r = data[idx];
    const g = data[idx + 1];
    const b = data[idx + 2];
    
    // Near white
    if (r >= threshold && g >= threshold && b >= threshold) return true;
    
    // Very faint shadows / light gray ground shadows connected to border
    if (removeShadows) {
      const max = Math.max(r, g, b);
      const min = Math.min(r, g, b);
      // light grayish (low saturation, brightness > 200)
      if (min > 195 && (max - min) < 22) return true;
    }
    return false;
  }

  // Push all border pixels to queue
  for (let x = 0; x < width; x++) {
    let idxTop = (0 * width + x) * 4;
    if (isBackground(idxTop)) {
      visited[x] = 1;
      queue.push(x, 0);
    }
    let idxBottom = ((height - 1) * width + x) * 4;
    if (isBackground(idxBottom)) {
      visited[(height - 1) * width + x] = 1;
      queue.push(x, height - 1);
    }
  }

  for (let y = 0; y < height; y++) {
    let idxLeft = (y * width + 0) * 4;
    if (isBackground(idxLeft)) {
      visited[y * width] = 1;
      queue.push(0, y);
    }
    let idxRight = (y * width + (width - 1)) * 4;
    if (isBackground(idxRight)) {
      visited[y * width + width - 1] = 1;
      queue.push(width - 1, y);
    }
  }

  let head = 0;
  while (head < queue.length) {
    const x = queue[head++];
    const y = queue[head++];
    const pIdx = (y * width + x) * 4;

    data[pIdx + 3] = 0;

    const neighbors = [
      [x + 1, y],
      [x - 1, y],
      [x, y + 1],
      [x, y - 1]
    ];

    for (let i = 0; i < 4; i++) {
      const nx = neighbors[i][0];
      const ny = neighbors[i][1];
      if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
        const nPos = ny * width + nx;
        if (!visited[nPos]) {
          visited[nPos] = 1;
          const nIdx = nPos * 4;
          if (isBackground(nIdx)) {
            queue.push(nx, ny);
          }
        }
      }
    }
  }

  await image.write(destPath);
  console.log(`Saved transparent sticker to: ${destPath}`);
}

async function run() {
  const brainDir = 'C:\\Users\\Samadhan\\.gemini\\antigravity-ide\\brain\\45b69b94-5fbd-4ad4-a572-6b1dfe324104';
  const outDir = 'C:\\Users\\Samadhan\\waste-watch\\frontend\\assets';

  const tasks = [
    {
      src: path.join(brainDir, 'dustbin_clean_1793093278176.jpg'),
      dest: path.join(outDir, 'sticker-dustbin.png'),
      threshold: 232,
      shadow: true
    },
    {
      src: path.join(brainDir, 'truck_sticker_1793093610662.jpg'),
      dest: path.join(outDir, 'sticker-truck.png'),
      threshold: 235,
      shadow: true
    },
    {
      src: path.join(brainDir, 'earth_recycle_1793093290948.jpg'),
      dest: path.join(outDir, 'sticker-earth.png'),
      threshold: 230,
      shadow: true
    },
    {
      src: path.join(brainDir, 'recycle_arrows_1793093337430.jpg'),
      dest: path.join(outDir, 'sticker-recycle-arrows.png'),
      threshold: 235,
      shadow: false
    },
    {
      src: path.join(brainDir, 'green_plant_1793093301587.jpg'),
      dest: path.join(outDir, 'sticker-plant.png'),
      threshold: 235,
      shadow: false
    },
    {
      src: path.join(brainDir, 'recycle_worker_1793093313650.jpg'),
      dest: path.join(outDir, 'sticker-worker.png'),
      threshold: 230,
      shadow: true
    },
    {
      src: path.join(brainDir, 'clean_city_1793093349973.jpg'),
      dest: path.join(outDir, 'sticker-city.png'),
      threshold: 235,
      shadow: true
    }
  ];

  for (const t of tasks) {
    if (fs.existsSync(t.src)) {
      await removeOuterWhiteBg(t.src, t.dest, t.threshold, t.shadow);
    }
  }
  console.log('Finished updating stickers!');
}

run().catch(console.error);
