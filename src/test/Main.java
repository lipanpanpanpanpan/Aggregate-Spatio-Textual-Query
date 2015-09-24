package test;

import java.util.List;

import annk.domain.GNNKQuery;
import annk.spatialindex.IRTree;
import documentindex.InvertedFile;
import io.QueryFileReader;
import io.ResultWriter;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.NNEntry;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Usage: GNNK index_file query_file topk alpha");
			System.exit(-1);
		}

		String indexFile = args[0];
		String queryFile = args[1];
		int topk = Integer.parseInt(args[2]);
		RTree.alpha_dist = Double.parseDouble(args[3]);

		PropertySet ps = new PropertySet();
		ps.setProperty("FileName", indexFile + ".rtree");
		IStorageManager diskfile = new DiskStorageManager(ps);
		
		PropertySet ps2 = new PropertySet();
		int indexIdentifier = 1; // (in this case I know that it is equal to 1)
		ps2.setProperty("IndexIdentifier", indexIdentifier);
		IRTree tree = new IRTree(ps2, diskfile);

		InvertedFile invertedFile = new InvertedFile(indexFile, 4096);

		int count = 0;
		long startTime = 0;
		long totalTime = 0;

		int ivIO = 0;
		int totalVisitedNodes = 0;

		QueryFileReader reader = new QueryFileReader(queryFile);
		List<GNNKQuery> gnnkQueries = reader.readGNNKQueries();
		
		startTime = System.currentTimeMillis();
		ResultWriter writer = new ResultWriter(gnnkQueries.size(), true);
		for (GNNKQuery q : gnnkQueries) {
			List<NNEntry> results = tree.gnnk(invertedFile, q, topk);
//			List<NNEntry> results = tree.gnnkBaseline(invertedFile, q, topk);
			writer.writeResult(results);

			totalVisitedNodes += tree.noOfVisitedNodes;
			ivIO += invertedFile.getIO();
			count++;
		}
		writer.write("Average nodes visited: " + totalVisitedNodes * 1.0 / count);
		writer.close();
		
		totalTime = System.currentTimeMillis() - startTime;

		System.out.println("Total time millisecond: " + totalTime);
		System.out.println("Average time millisecond: " + totalTime * 1.0 / count);
		System.out.println("Average total IO: " + (tree.getIO() + ivIO) * 1.0 / count);
		System.out.println("Average tree IO: " + tree.getIO() * 1.0 / count);
		System.out.println("Average inverted index IO: " + ivIO * 1.0 / count);
	}
}